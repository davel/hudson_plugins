package hudson.plugins.testabilityexplorer.publisher;

import hudson.model.AbstractProject;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModule;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.StaplerRequest;
import net.sf.json.JSONObject;

/**
 * A {@link BuildStepDescriptor} for the testability explorer plugin.
 *
 * @author reik.schatz
 */
public class TestabilityExplorerDescriptor extends BuildStepDescriptor<Publisher>
{
    public static final String DISPLAY_NAME = "Publish Testability Explorer Report";

    public TestabilityExplorerDescriptor() {
        super(FreestylePublisher.class);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return !MavenModuleSet.class.isAssignableFrom(aClass) && !MavenModule.class.isAssignableFrom(aClass);
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException
    {
        return req.bindJSON(FreestylePublisher.class, formData);
    }
}
