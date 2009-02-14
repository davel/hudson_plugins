package org.wirsind.info;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormFieldValidator;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.QueryParameter;

import com.sun.org.apache.bcel.internal.generic.IF_ICMPGE;

import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link RhapsodyBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
 * will be invoked. 
 *
 * @author Markus Hoffmann
 */
public class RhapsodyBuilder extends Builder {

	private final String components;
	private String projectPath;
	private final String rhapsodyConfiguration;
	private final boolean generate;
	private final boolean make;
	
    @DataBoundConstructor
    public RhapsodyBuilder(String components, String projectPath, String rhapsodyConfiguration, boolean generate, boolean make) {
    	this.components = components;
		this.projectPath = projectPath;
		this.rhapsodyConfiguration = rhapsodyConfiguration;
		this.generate = generate;
		this.make = make;

    }

	public String getProjectPath()
	{
		return projectPath;
	}
	public String getComponents()
	{
		return components;
	}
	public String getRhapsodyConfiguration()
	{
		return rhapsodyConfiguration;
	}
	public boolean getGenerate()
	{
		return generate;
	}
	public boolean getMake()
	{
		return make;
	}

	
	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener)
	{
		// TODO Auto-generated method stub
		/*try {
			build.getExecutor().sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return true;
	}
    
    public boolean perform(Build build, Launcher launcher, BuildListener listener) {
    	
    	// resolving hudson variables for the properties set
    	Map<String, String> envVars = build.getEnvVars();
    	Set<String> keys = envVars.keySet();
    	for (String key : keys) {
    	   this.projectPath   = this.projectPath.replaceAll("\\$" + key, envVars.get(key));
    	   this.projectPath   = this.projectPath.replaceAll("\\$\\{" + key + "\\}", envVars.get(key));
    	}

    	// split the components entered in the component field
    	String[] componentList = this.components.split(",");

    	String rhapsodyCall = DESCRIPTOR.rhapsodyClPath() + " -cmd=open " + this.projectPath;
    	for (int i=0; i < componentList.length; ++i)
    	{
			if (generate)
			{
    			rhapsodyCall += " -cmd=setcomponent " + componentList[i];
    			rhapsodyCall += " -cmd=setconfiguration " + this.rhapsodyConfiguration + " -cmd=regenerate";
	    		if (make)
	    		{
	    			rhapsodyCall += " -cmd=syncmake";
	    		}
			}
    	}
    	rhapsodyCall += " -cmd=exit \n";
    	
    	Map<String, String> system =System.getenv();
		List<String> param = new ArrayList<String>();
		for (Map.Entry<String, String> entry : system.entrySet()) {
			String s = entry.getKey() + "=" + entry.getValue();			
			param.add(s);
		}
		if (DESCRIPTOR.licenseServerPath()!=null) {
			param.add("LM_LICENSE_FILE=" + DESCRIPTOR.licenseServerPath());
		}
		String[] env = param.toArray(new String[param.size()]);	
    	
		int returnValue;
    	try {
			Proc proc = launcher.launch(rhapsodyCall, env, listener.getLogger(), build.getProject().getWorkspace());
			returnValue = proc.join();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		listener.getLogger().println("IOException !");
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			listener.getLogger().println("InterruptedException !");
			return false;
		}
		if (returnValue == 0)
    	    return true;
		else
			return false;
    }

    public Descriptor<Builder> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link RhapsodyBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/RhapsodyBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    public static final class DescriptorImpl extends Descriptor<Builder> {

        private String rhapsodyClPath;
        private String licenseServerPath;

        DescriptorImpl() {
            super(RhapsodyBuilder.class);
            load();
        }

        public String rhapsodyClPath() {
        	return rhapsodyClPath;
        }
        
        public String licenseServerPath() {
        	return licenseServerPath;
        }
        
        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This receives the current value of the field.
         */
        public void doCheckProjectPath(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,null) {
                /**
                 * The real check goes here. In the end, depending on which
                 * method you call, the browser shows text differently.
                 */
                protected void check() throws IOException, ServletException {
                    if(value.length()==0)
                        error("please set the path to the Rhapsody Project !");
                    else
                    if(value.length()<4)
                        warning("isn't the path too short?");
                    else {
                    	File file = new File(value);
                    	if (file.isDirectory()) {
                    		error("you entered a directory please select the *.rpy file !");
                    	} else 
                    		//TODO add more checks
                    		ok();
                    }

                }
            }.process();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This receives the current value of the field.
         */
        public void doCheckComponents(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,null) {
                /**
                 * The real check goes here. In the end, depending on which
                 * method you call, the browser shows text differently.
                 */
                protected void check() throws IOException, ServletException {
                    if(value.length()==0)
                        error("please specify at least one component");
                    else {
                    		//TODO add more checks
                    		ok();
                    }
                }
            }.process();
        }  

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This receives the current value of the field.
         */
        public void doCheckRhapsodyConfiguration(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,null) {
                /**
                 * The real check goes here. In the end, depending on which
                 * method you call, the browser shows text differently.
                 */
                protected void check() throws IOException, ServletException {
                    if(value.length()==0)
                        error("please specify a configuration");
                    else {
                    		//TODO add more checks
                    		ok();
                    }
                }
            }.process();
        } 
        
        /**
         * Performs on-the-fly validation of the form field 'licenseServerPath'.
         *
         * @param value
         *      This receives the license server address or the path to the license file of Rhapsody.
         */
        public void doCheckLicenseServerPath(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,null) {
                /**
                 * The real check goes here. In the end, depending on which
                 * method you call, the browser shows text differently.
                 */
                protected void check() throws IOException, ServletException {
                    if(value.length()==0)
                        error("please type in the License Server");
                    else {
                    	//TODO add more checks
                    	ok();
                    }
                }
            }.process();
        } 
       
        /**
         * Performs on-the-fly validation of the form field 'rhapsodyCLPath'.
         *
         * @param value
         *      This receives the path to the executable rhapsodyCL.exe
         */
        public void doCheckRhapsodyClPath(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,null) {
                /**
                 * The real check goes here. In the end, depending on which
                 * method you call, the browser shows text differently.
                 */
                protected void check() throws IOException, ServletException {
                    if(value.length()==0)
                        error("please specify the path to the RhapsodyCL.exe file");
                    else if(!value.contains("RhapsodyCL.exe")) {
                    	error("didn't find RhapsodyCL.exe in the path !");
                    }else{
                    	ok();                   	
                    }
                }
            }.process();
        }         
        
        
        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Rhapsody Build";
        }

        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            // to persist global configuration information,
        	rhapsodyClPath = o.getString("rhapsodyClPath");
        	licenseServerPath = o.getString("licenseServerPath");
            save();
            return super.configure(req, o);
        } 
    }
}


