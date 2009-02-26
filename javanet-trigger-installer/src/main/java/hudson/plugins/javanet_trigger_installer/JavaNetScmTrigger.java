package hudson.plugins.javanet_trigger_installer;

import hudson.model.Item;
import hudson.model.SCMedItem;
import hudson.plugins.javanet_trigger_installer.Task.Update;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link Trigger} for java.net CVS change notification e-mail.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaNetScmTrigger extends Trigger<SCMedItem> {
    @Override
    public void start(SCMedItem project, boolean newInstance) {
        super.start(project, newInstance);
        if(newInstance)
            new Update(project.asProject()).scheduleHighPriority();
    }

    @Override
    public void stop() {
        super.stop();
        new Update(job.asProject()).scheduleHighPriority();
    }

    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {
        public boolean isApplicable(Item item) {
            return item instanceof SCMedItem;
        }

        public String getDisplayName() {
            return "Monitor changes in java.net CVS/SVN repository";
        }

        public String getHelpFile() {
            return "/plugin/javanet-trigger-installer/help.html";
        }
    }
}
