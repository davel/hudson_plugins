package hudson.plugins.cmake;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes <tt>cmake</tt> as the build process.
 *
 *
 * @author Volker Kaiser
 */
public class CmakeBuilder extends Builder {

	private static final String CMAKE = "cmake";
	
	private String sourceDir;
    private String buildDir;
    private String installDir;
    private String buildType;
    private String generator;
    private String makeCommand;
    private String installCommand;
    private String preloadScript;
    private String cmakeArgs;
    private boolean cleanBuild;

    private CmakeBuilderImpl builderImpl;

    
    @DataBoundConstructor
    public CmakeBuilder(String sourceDir, 
    		String buildDir, 
    		String installDir, 
    		String buildType, 
    		boolean cleanBuild,
    		String generator, 
    		String makeCommand, 
    		String installCommand,
    		String preloadScript,
    		String cmakeArgs) {
    	this.sourceDir = sourceDir;
		this.buildDir = buildDir;
		this.installDir = installDir;
		this.buildType = buildType;
		this.cleanBuild = cleanBuild;
		this.generator = generator;
		this.makeCommand = makeCommand;
		this.installCommand = installCommand; 		
		this.cmakeArgs = cmakeArgs;
		this.preloadScript = preloadScript;
		builderImpl = new CmakeBuilderImpl();
    }

    public String getSourceDir() {
    	return this.sourceDir;
    }
    
    public String getBuildDir() {
		return this.buildDir;
	}

    public String getInstallDir() {
    	return this.installDir;
    }

    public String getBuildType() {
    	return this.buildType;
    }
    
    public boolean getCleanBuild() {
    	return this.cleanBuild;
    }
    
    public String getGenerator() {
    	return this.generator;
    }
    
    public String getMakeCommand() {
    	return this.makeCommand;
    }
    
    public String getInstallCommand() {
    	return this.installCommand;
    }
    
    public String getPreloadScript() {
    	return this.preloadScript;
    }
    
    public String getCmakeArgs() {
    	return this.cmakeArgs;
    }
    
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    	listener.getLogger().println("MODULE: " + build.getModuleRoot());
    	
    	if (builderImpl == null) {
    		builderImpl = new CmakeBuilderImpl();
    	}
        EnvVars envs = build.getEnvironment(listener);
        FilePath workDir = new FilePath(build.getProject().getWorkspace(), this.buildDir);
        
        String theSourceDir;
    	String theInstallDir;
    	try {
    		if (this.cleanBuild) {
    			listener.getLogger().println("Cleaning Build Dir... " + workDir.toString());
    			builderImpl.preparePath(envs, this.buildDir, 
    					CmakeBuilderImpl.PreparePathOptions.CREATE_NEW_IF_EXISTS);
    		} else {
    			builderImpl.preparePath(envs, this.buildDir,
    					CmakeBuilderImpl.PreparePathOptions.CREATE_IF_NOT_EXISTING);
    		}    			
    		theSourceDir = builderImpl.preparePath(envs, this.sourceDir,
    				CmakeBuilderImpl.PreparePathOptions.CHECK_PATH_EXISTS);
    		theInstallDir = builderImpl.preparePath(envs, this.installDir,
    				CmakeBuilderImpl.PreparePathOptions.CREATE_NEW_IF_EXISTS);
    	} catch (IOException ioe) {
    		listener.getLogger().println(ioe.getMessage());
    		return false;
    	}
//    	catch (InterruptedException e) {
//    		listener.getLogger().println(e.getMessage());
//			return false;
//		}
    	
    	String cmakeBin = checkCmake(listener);
    	String cmakeCall = builderImpl.buildCMakeCall(cmakeBin, this.generator, this.preloadScript, theSourceDir, theInstallDir, buildType, cmakeArgs);
    	listener.getLogger().println("Build dir  : " + workDir.toString());
    	listener.getLogger().println("CMake call : " + cmakeCall);

    	try {
    		int result = launcher.launch().cmds(Util.tokenize(cmakeCall)).envs(envs).stdout(listener).pwd(workDir).join();
    		if (result != 0) {
    			return false;
    		}
    		
    		if (!getMakeCommand().trim().isEmpty()) {
    			result = launcher.launch().cmds(Util.tokenize(getMakeCommand())).envs(envs).stdout(listener).pwd(workDir).join();
    			if (result != 0) {
    				return false;
    			}
    		}
    		if (!getInstallCommand().trim().isEmpty()) {
    			result = launcher.launch().cmds(Util.tokenize(getInstallCommand())).envs(envs).stdout(listener).pwd(workDir).join();
    		}
    		return (result == 0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		return false;
    }

	private String checkCmake(BuildListener listener) throws IOException,
			InterruptedException {
		String cmakeBin = CMAKE;
        String cmakePath = getDescriptor().cmakePath();
        if (cmakePath != null && cmakePath.length() > 0) {
    		cmakeBin = cmakePath;
    	}
        Process cmakeProc = new ProcessBuilder(cmakeBin, "-version").start();
        BufferedReader cmakeProcReader = new BufferedReader(new InputStreamReader(cmakeProc.getInputStream()));
        String temp = cmakeProcReader.readLine();
        while (temp != null) {
        	listener.getLogger().println(temp);
        	temp = cmakeProcReader.readLine();
        }
        cmakeProc.waitFor();
		return cmakeBin;
	}

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link CmakeBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/CmakeBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String cmakePath;
        private transient List<String> allowedBuildTypes;
        private transient String errorMessage;
        
        public DescriptorImpl() {
            super(CmakeBuilder.class);
            load();
            this.allowedBuildTypes = new ArrayList<String>();            
            this.allowedBuildTypes.add("Debug");
            this.allowedBuildTypes.add("Release");
            this.allowedBuildTypes.add("RelWithDebInfo");
            this.allowedBuildTypes.add("MinSizeRel");
            this.errorMessage = "Must be one of Debug, Release, RelWithDebInfo, MinSizeRel";
        }
        
        public FormValidation doCheckSourceDir(@AncestorInPath AbstractProject project, @QueryParameter final String value) throws IOException, ServletException {
            FilePath ws = project.getSomeWorkspace();
            if(ws==null) return FormValidation.ok();
            return ws.validateRelativePath(value,true,false);
        }
        
        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         */
        public FormValidation doCheckBuildDir(@QueryParameter final String value) throws IOException, ServletException {
            if(value.length()==0)
                return FormValidation.error("Please set a build directory");
            if(value.length() < 1)
                return FormValidation.warning("Isn't the name too short?");

            File file = new File(value);
            if (file.isFile())
                return FormValidation.error("build dir is a file");

            //TODO add more checks
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'buildType'.
         *
         * @param value
         */
        public FormValidation doCheckBuildType(@QueryParameter final String value) throws IOException, ServletException {
            for (String allowed : DescriptorImpl.this.allowedBuildTypes)
                if (value.equals(allowed))
                    return FormValidation.ok();
            if (value.length() > 0)
                return FormValidation.error(DescriptorImpl.this.errorMessage);

            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'makeCommand'.
         *
         * @param value
         */
        public FormValidation doCheckMakeCommand(@QueryParameter final String value) throws IOException, ServletException {
            if (value.length() == 0) {
            	return FormValidation.error("Please set make command");
            }
            return FormValidation.validateExecutable(value);
        }

        
        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "CMake Build";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            cmakePath = o.getString("cmakePath");
            save();
            return super.configure(req, o);
        }

        public String cmakePath() {
        	return cmakePath;
        }
        
        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        	return req.bindJSON(CmakeBuilder.class, formData);
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        	return FreeStyleProject.class.isAssignableFrom(jobType);
        }
    }
}

