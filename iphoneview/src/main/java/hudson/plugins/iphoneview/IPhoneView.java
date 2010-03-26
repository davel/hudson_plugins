package hudson.plugins.iphoneview;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.model.ViewDescriptor;
import hudson.tasks.test.TestResultProjectAction;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * View for iPhone.iPod touch
 * 
 * @author Seiji Sogabe
 */
public class IPhoneView<P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>>  extends ListView {

    @DataBoundConstructor
    public IPhoneView(final String name) {
        super(name);
    }

    public boolean hasJobTestResult(final String name) {
        final P project = getProject(name);
        final TestResultProjectAction action =  project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return false;
        }
        return action.getLastTestResultAction().getPreviousResult() != null;
    }

    public IPhoneJob<P, B> getIPhoneJob(final String name) {
        final P project = getProject(name);
        return new IPhoneJob<P, B>(project);
    }

    @SuppressWarnings("unchecked")
    private P getProject(final String name) {
        final TopLevelItem item = getJob(name);
        if (item == null || !(AbstractProject.class.isAssignableFrom(item.getClass()))) {
            throw new IllegalArgumentException("failed to get Job.");
        }
        return (P) item;
    }

    @Extension
    public static class DesciptorImpl extends ViewDescriptor {
        @Override
        public String getDisplayName() {
            return "iPhone/iPod touch View";
        }
    }

}
