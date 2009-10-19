package hudson.plugins.kundo;

import hudson.tasks.Builder;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.CopyOnWrite;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;

/**
 * I have to recognise all the other Hudson Plugin developers
 * without their source code I wouldn't have had any idea what
 * was going on. Thanks for the inspiration, and in several
 * places the code.
 */

public class Kundo extends Builder {
    /**
     * The phases, optional properties, and other Kundo options.
     */
    private final String phases;

    /**
     * properties specified in the advanced section
     */
    private final String properties;

    /**
     * Identifies {@link KundoInstallation} to be used.
     */
    private final String kundoName;

    @DataBoundConstructor
    public Kundo( String phases, String properties, String kundoName ) {
        this.phases = phases;
        this.properties = properties;
        this.kundoName = kundoName;
    }

    public String getProperties() {
        return properties;
    }

    public String getPhases() {
        return phases;
    }

    /**
     * Gets the Kundo kernel to call,
     * or null if only one installation exists.
     */
    public KundoInstallation getKundo() {
        for( KundoInstallation i : DESCRIPTOR.getInstallations() ) {
            if( kundoName != null && i.getName().equals( kundoName ) )
                return i;
        }
        return null;
    }

    public boolean perform( AbstractBuild<?,?> build, Launcher launcher, BuildListener listener ) throws IOException, InterruptedException {
        ArgumentListBuilder args = new ArgumentListBuilder();

        String execType;
        if( launcher.isUnix() ){
            execType = "kundo";
        }else{
            execType = "kundo.bat";
	}

        String normalizedPhases = phases.replaceAll( "[\t\r\n]+"," " );

        String normalizedProperties = properties.replaceAll( "[\t\r\n]+"," " );
	String[] splitProperties = normalizedProperties.split( " " );
	HashMap propMap = new HashMap();
	for( String property : splitProperties ){
		if( property.indexOf( "=" ) != -1 ){
			String[] splitProp = property.split( "=" );
			if( splitProp.length == 2 ){
				propMap.put( splitProp[0], splitProp[1] );
			}
		}
	}

        KundoInstallation currentInstall = getKundo();
        if( currentInstall==null ) {
            args.add( execType );
        }else{
            File exec = currentInstall.getExecutable();
            if( !currentInstall.getExists() ) {
                listener.fatalError( exec + " doesn't exist" );
                return false;
            }
            args.add( exec.getPath() );
        }
	if( !propMap.isEmpty() ){
        	args.addKeyValuePairs( "-D", propMap );
	}
        args.addKeyValuePairs( "-D", build.getBuildVariables() );
        args.addTokenized( normalizedPhases );

        Map<String,String> env = build.getEnvironment(listener);
        if( currentInstall != null )
            env.put( "KUNDO_HOME", currentInstall.getKundoHome() );

        if( !launcher.isUnix() ) {
            args.prepend( "cmd.exe","/C" );
            args.add( "&&", "exit", "%%ERRORLEVEL%%" );
        }

        try {
            int r = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();
            return r == 0;
        } catch ( IOException e ) {
            Util.displayIOException( e, listener );
            e.printStackTrace( listener.fatalError( "command execution failed" ) );
            return false;
        }
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {
    	
        @CopyOnWrite
        private volatile KundoInstallation[] installations = new KundoInstallation[0];

        private DescriptorImpl() {
            super( Kundo.class );
            load();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/kundo/help.html";
        }

        public String getDisplayName() {
            return "Invoke Kundo job";
        }

        public KundoInstallation[] getInstallations() {
            return installations;
        }

        @Override
        public boolean configure( StaplerRequest req, JSONObject formData ) {
            installations = req.bindParametersToList( KundoInstallation.class, "kundo." ).toArray( new KundoInstallation[0] );
            save();
            return true;
        }

        /**
         * Checks if the specified Hudson KUNDO_HOME is valid.
         */
        public FormValidation doCheckKundoHome(@QueryParameter String value) {
            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return FormValidation.ok();
            File f = new File(Util.fixNull(value));

            if( !f.isDirectory() ) {
                return FormValidation.error( f + " is not a directory" );
            }

            if( !new File( f, "bin" ).exists() && !new File( f, "lib" ).exists() && !new File( f, "conf" ).exists() && !new File( f, "groovy" ).exists() ) {
                return FormValidation.error( f + " isn't a proper Kundo kernel" );
            }

            if( !new File( f, "bin/kundo" ).exists() ) {
                return FormValidation.error( f + " isn't a proper Kundo kernel" );
            }

            return FormValidation.ok();
        }
    }

}
