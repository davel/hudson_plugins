package hudson.plugins.kundo;

import hudson.tasks.Builder;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Descriptor;
import hudson.Launcher;
import hudson.Util;
import hudson.CopyOnWrite;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;

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

    public boolean perform( Build<?,?> build, Launcher launcher, BuildListener listener ) throws InterruptedException {
        Project proj = build.getProject();

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

        Map<String,String> env = build.getEnvVars();
        if( currentInstall != null )
            env.put( "KUNDO_HOME", currentInstall.getKundoHome() );

        if( !launcher.isUnix() ) {
            args.prepend( "cmd.exe","/C" );
            args.add( "&&", "exit", "%%ERRORLEVEL%%" );
        }

        try {
            int r = launcher.launch( args.toCommandArray(), env, listener.getLogger(), proj.getModuleRoot() ).join();
            return r == 0;
        } catch ( IOException e ) {
            Util.displayIOException( e, listener );
            e.printStackTrace( listener.fatalError( "command execution failed" ) );
            return false;
        }
    }

    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {
    	
        @CopyOnWrite
        private volatile KundoInstallation[] installations = new KundoInstallation[0];

        private DescriptorImpl() {
            super( Kundo.class );
            load();
        }

        public String getHelpFile() {
            return "/plugin/kundo/help.html";
        }

        public String getDisplayName() {
            return "Invoke Kundo job";
        }

        public KundoInstallation[] getInstallations() {
            return installations;
        }

        public boolean configure( StaplerRequest req ) {
            installations = req.bindParametersToList( KundoInstallation.class, "kundo." ).toArray( new KundoInstallation[0] );
            save();
            return true;
        }

        /**
         * Checks if the specified Hudson KUNDO_HOME is valid.
         */
        public void doCheckKundoHome( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
            
        	
            new FormFieldValidator( req, rsp ,true ) {
                public void check() throws IOException, ServletException {
                    File f = getFileParameter( "value" );
                    
                    if( !f.isDirectory() ) {
                        error( f + " is not a directory" );
                        return;
                    }
                    
                    if( !new File( f, "bin" ).exists() && !new File( f, "lib" ).exists() && !new File( f, "conf" ).exists() && !new File( f, "groovy" ).exists() ) {
                        error( f + " isn't a proper Kundo kernel" );
                        return;
                    }

                    if( !new File( f, "bin/kundo" ).exists() ) {
                        error( f + " isn't a proper Kundo kernel" );
                        return;
                    }

                    ok();
                }
            }.process();
        }
    }

}
