package hudson.plugins.codeplex;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Property for {@link AbstractProject} that stores the associated CodePlex project name.
 *
 * @author Erik Ramfelt
 */
public final class CodePlexProjectProperty extends JobProperty<AbstractProject<?,?>> {

    private static final String CODEPLEX_URL_STR = "http://www.codeplex.com/";

    /**
     * CodePlex project name.
     * 
     * Null if this is not configured yet.
     */
    public final String projectName;
    
    @DataBoundConstructor
    public CodePlexProjectProperty(String projectName) {
        // normalize
        this.projectName = Util.fixEmptyAndTrim(projectName);
    }

    /**
     * Returns the project name for the codeplex project property
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }
    
    public String getProjectUrlString() {
        return CODEPLEX_URL_STR + projectName + "/";
    }

    public String getSubversionRootUrlString() {
        return String.format("https://%s.svn.codeplex.com/svn/", projectName);
    }
    
    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(CodePlexProjectProperty.class);
            load();
        }

        @SuppressWarnings("unchecked") // because of the raw type in the method declaration
        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return "CodePlex project name";
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (Util.fixEmptyAndTrim(formData.getString("projectName")) != null) {
                return super.newInstance(req, formData);
            }
            return null;
        }
    }
}
