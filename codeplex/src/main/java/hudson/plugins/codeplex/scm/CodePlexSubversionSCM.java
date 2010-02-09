package hudson.plugins.codeplex.scm;

import java.util.Arrays;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Util;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.codeplex.browsers.CodePlexSubversionBrowser;
import hudson.scm.SubversionSCM;

/**
 * Source code manager that is extends the SubversionSCM and simplifies the configuration.
 * 
 * @author redsolo
 */
public class CodePlexSubversionSCM extends SubversionSCM {

    private static final long serialVersionUID = 1L;

    private String directory;
    
    @DataBoundConstructor
    public CodePlexSubversionSCM(String directory, ModuleLocation locations) {
        super(Arrays.asList(locations), true, new CodePlexSubversionBrowser(), "");
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

    @Extension
    public static class DescriptorImpl extends SubversionSCM.DescriptorImpl {

        public static final String DISPLAY_NAME = "CodePlex (automatic configuration using subversion)";

		public DescriptorImpl() {
            super(CodePlexSubversionSCM.class, CodePlexSubversionBrowser.class);       
        }
        
        @Override
        public CodePlexSubversionSCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String projectName = req.getParameter("codeplex.projectName");
            String directory = req.getParameter("codeplex.svnRemoteDirectory");
            if (Util.fixEmptyAndTrim(projectName) == null) {
                throw new IllegalArgumentException("The CodePlex project name field can not be empty when selecting the CodePlex Subversion SCM.");
            }
            if (Util.fixEmptyAndTrim(directory) == null) {
                directory = "trunk";
            }
            return newInstance(new CodePlexProjectProperty(projectName), directory);
        }
        
        public static CodePlexSubversionSCM newInstance(CodePlexProjectProperty property, String remoteDirectory) {
            return new CodePlexSubversionSCM(remoteDirectory, new ModuleLocation(property.getSubversionRootUrlString() + remoteDirectory, "."));
        }

        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }
    }
}
