package hudson.plugins.googlecode.scm;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Util;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.plugins.googlecode.GoogleCodeRepositoryBrowser;
import hudson.scm.SubversionSCM;

/**
 * Source code manager that is auto configured from the GoogleCodeProjectProperty.
 * It will create a Subversion SCM field and use it for all SCM methods. The 
 * SVN url is built using the name of the project.
 * 
 * @author Erik Ramfelt
 */
public class GoogleCodeSCM extends SubversionSCM {

    private static final long serialVersionUID = 1L;

    private String directory;
    
    @DataBoundConstructor
    public GoogleCodeSCM(String directory, List<ModuleLocation> locations) {
        super(locations, true, new GoogleCodeRepositoryBrowser(new GoogleCodeProjectProperty.PropertyRetrieverImpl()), "");
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
            super(GoogleCodeSCM.class, GoogleCodeRepositoryBrowser.class);       
        }
        
        @Override
        public GoogleCodeSCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            //return req.bindJSON(GoogleCodeSCM.class, formData);
            String projectWebsite = req.getParameter("googlecode.googlecodeWebsite");
            String directory = req.getParameter("googlecode.svnRemoteDirectory");
            List<ModuleLocation> moduleLocations = getModuleLocations(projectWebsite, directory, ".");
            return new GoogleCodeSCM(directory, moduleLocations);
        }

        public static List<ModuleLocation> getModuleLocations(String googlecodeWebsite, String remoteDirectry, String localDirectory) {
            GoogleCodeProjectProperty property = new GoogleCodeProjectProperty(googlecodeWebsite);
            ModuleLocation location = new ModuleLocation(property.getSubversionRootUrl() + remoteDirectry, localDirectory);
            return Arrays.asList(location);
        }

        @Override
        public String getDisplayName() {
            return "Google Code (automatic configuration)";
        }
    }
}
