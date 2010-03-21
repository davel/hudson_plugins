package hudson.plugins.iphoneview;

import hudson.Extension;
import hudson.model.Job;
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
public class IPhoneView extends ListView {

    @DataBoundConstructor
    public IPhoneView(String name) {
        super(name);
    }

    public boolean hasJobTestResult(String name) {
        TopLevelItem item = getJob(name);
        if (item == null || !(item instanceof Job<?, ?>)) {
            throw new IllegalArgumentException("failed to get Job.");
        }
        TestResultProjectAction action =  ((Job<?, ?>) item).getAction(TestResultProjectAction.class);
        if (action == null) {
            return false;
        }
        return action.getLastTestResultAction().getPreviousResult() != null;
    }

    @Extension
    public static class DesciptorImpl extends ViewDescriptor {
        @Override
        public String getDisplayName() {
            return "iPhone/iPod touch View";
        }
    }

}
