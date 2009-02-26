package hudson.plugins.googlecode.scm;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.plugins.googlecode.GoogleCodeRepositoryBrowser;
import hudson.plugins.googlecode.PluginImpl;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SubversionSCM;

/**
 * Source code manager that is auto configured from the GoogleCodeProjectProperty.
 * It will create a Subversion SCM field and use it for all SCM methods. The 
 * SVN url is built using the name of the project.
 * 
 * @author Erik Ramfelt
 */
public class GoogleCodeSCM extends SCM {

    private transient SCM configuredScm;
    private String directory;
    
    @DataBoundConstructor
    public GoogleCodeSCM(String directory) {
        this.directory = directory;
    }

    /**
     * Returns the directory that will retrieved from the SVN repository.
     * Default is "trunk"
     * @return the directory
     */
    public String getDirectory() {
        if (Util.fixEmptyAndTrim(directory) == null) {
            directory = "trunk";
        }
        return directory;
    }

    /**
     * Get the SCM, create one if there is none.
     * The SCM is created lazily as the project (that this SCM belongs to)
     * may not be created when the SCM is created.
     * @return the SCM object (SubversionSCM)
     */
    private SCM getSCM() {
        if (configuredScm == null) {
            for (AbstractProject<?, ?> project : Hudson.getInstance().getItems(AbstractProject.class)) {
                if (this == project.getScm() ) {
                    GoogleCodeProjectProperty property = project.getProperty(GoogleCodeProjectProperty.class);
                    if (property != null) {

                        String path = directory;
                        String[] remoteLocations = new String[] {"http://" + property.getProjectName() + ".googlecode.com/svn/" + path};
                        String[] localLocations = new String[] {"."};
                        configuredScm = new SubversionSCM(remoteLocations, localLocations, true, new GoogleCodeRepositoryBrowser(PluginImpl.PROJECT_PROPERTY_DESCRIPTOR),null);
                    } else {
                        throw new RuntimeException("The project does not have a google code property. Please report this to the plugin author.");
                    }
                    break;
                }
            }
            if (configuredScm == null) {
                throw new RuntimeException("Could not find the project for this SCM object. Please contact plugin author.");
            }
        }
        return configuredScm;        
    }
    
    @Override
    public void buildEnvVars(AbstractBuild build, Map<String, String> env) {
        getSCM().buildEnvVars(build, env);
    }
    
    @Override
    public RepositoryBrowser getBrowser() {
        return getSCM().getBrowser();
    }

    @Override
    public FilePath getModuleRoot(FilePath workspace) {
        return getSCM().getModuleRoot(workspace);
    }
    
    @Override
    public FilePath[] getModuleRoots(FilePath workspace) {
        return getSCM().getModuleRoots(workspace);
    }
    
    @Override
    public boolean requiresWorkspaceForPolling() {
        return getSCM().requiresWorkspaceForPolling();
    }

    @Override
    public boolean supportsPolling() {
        return getSCM().supportsPolling();
    }

    @Override
    public boolean checkout(AbstractBuild arg0, Launcher arg1, FilePath arg2, BuildListener arg3, File arg4) throws IOException, InterruptedException {
        return getSCM().checkout(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public boolean pollChanges(AbstractProject arg0, Launcher arg1, FilePath arg2, TaskListener arg3) throws IOException, InterruptedException {
        return getSCM().pollChanges(arg0, arg1, arg2, arg3);
    }
    
    @Override
    public ChangeLogParser createChangeLogParser() {
        return getSCM().createChangeLogParser();
    }

    @Override
    public SCMDescriptor<?> getDescriptor() {
        return PluginImpl.GOOGLE_CODE_SCM_DESCRIPTOR;
    }
    
    public static class DescriptorImpl extends SCMDescriptor<GoogleCodeSCM> {

        public DescriptorImpl() {
            super(GoogleCodeSCM.class, GoogleCodeRepositoryBrowser.class);       
        }
        
        @Override
        public GoogleCodeSCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(GoogleCodeSCM.class, formData);
        }

        @Override
        public String getDisplayName() {
            return "Google Code (automatic configuration)";
        }
    }
}
