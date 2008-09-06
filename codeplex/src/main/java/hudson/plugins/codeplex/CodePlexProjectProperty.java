package hudson.plugins.codeplex;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;

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

    @Override
    public DescriptorImpl getDescriptor() {
        return PluginImpl.PROJECT_PROPERTY_DESCRIPTOR;
    }

    @Override
    public Action getJobAction(AbstractProject<?, ?> job) {
        if (projectName != null) {
            return new CodePlexLinkProjectAction(this);
        } else {
            return null;
        }
    }
    
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(CodePlexProjectProperty.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return "CodePlex project name";
        }
    }
}
