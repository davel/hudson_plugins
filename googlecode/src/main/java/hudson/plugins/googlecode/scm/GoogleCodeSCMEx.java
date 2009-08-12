package hudson.plugins.googlecode.scm;

import java.util.Arrays;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Util;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.plugins.googlecode.GoogleCodeRepositoryBrowser;
import hudson.scm.SubversionSCM;

/**
 * Source code manager that is extends the SubversionSCM and simplifies the configuration.
 * The extended class was introduced because of issue 4136 where other plugins 
 * and subversion parts assume the SCM to a project is a SubversionSCM class. 
 * 
 * @author redsolo
 */
public class GoogleCodeSCMEx extends SubversionSCM {

    private static final long serialVersionUID = 1L;

    private String directory;
    
    @DataBoundConstructor
    public GoogleCodeSCMEx(String directory, ModuleLocation locations) {
        super(Arrays.asList(locations), true, new GoogleCodeRepositoryBrowser(new GoogleCodeProjectProperty.PropertyRetrieverImpl()), "");
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

        public DescriptorImpl() {
            super(GoogleCodeSCMEx.class, GoogleCodeRepositoryBrowser.class);       
        }
        
        @Override
        public GoogleCodeSCMEx newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String projectWebsite = req.getParameter("googlecode.googlecodeWebsite");
            String directory = req.getParameter("googlecode.svnRemoteDirectory");
            if (Util.fixEmptyAndTrim(projectWebsite) == null) {
                throw new IllegalArgumentException("The Google Code project site field can not be empty when selecting the Google Code SCM.");
            }
            if (Util.fixEmptyAndTrim(directory) == null) {
                directory = "trunk";
            }
            return newInstance(new GoogleCodeProjectProperty(projectWebsite), directory);
        }
        
        public static GoogleCodeSCMEx newInstance(GoogleCodeProjectProperty property, String remoteDirectory) {
            return new GoogleCodeSCMEx(remoteDirectory, new ModuleLocation(property.getSubversionRootUrl() + remoteDirectory, "."));
        }

        @Override
        public String getDisplayName() {
            return "Google Code (automatic configuration)";
        }
    }
}
