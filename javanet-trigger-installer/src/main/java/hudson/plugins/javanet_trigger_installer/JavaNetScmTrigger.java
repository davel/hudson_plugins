package hudson.plugins.javanet_trigger_installer;

import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.plugins.javanet_trigger_installer.Task.Update;
import hudson.triggers.Trigger;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link Trigger} for java.net CVS change notification e-mail.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaNetScmTrigger extends Trigger {

    public Descriptor<Trigger> getDescriptor() {
        return DESCRIPTOR;
    }


    public void start(Project project, boolean newInstance) {
        super.start(project, newInstance);
        if(newInstance)
            new Update(project).scheduleHighPriority();
    }

    public void stop() {
        super.stop();
        new Update(project).scheduleHighPriority();
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Trigger> {
        public DescriptorImpl() {
            super(JavaNetScmTrigger.class);
        }

        public String getDisplayName() {
            return "Monitor changes in java.net CVS/SVN repository";
        }

        public String getHelpFile() {
            return "/plugin/javanet-trigger-installer/help.html";
        }

        public Trigger newInstance(StaplerRequest staplerRequest) throws FormException {
            return new JavaNetScmTrigger();
        }
    }
}
