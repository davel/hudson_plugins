package hudson.plugins.javanet_trigger_installer;

import hudson.model.Hudson;
import hudson.model.PeriodicWork;
import hudson.model.Project;
import hudson.plugins.javanet_trigger_installer.Task.Check;
import hudson.Extension;
import hudson.util.TimeUnit2;

/**
 * Runs periodically to update the project setting
 * from the actual setting in java.net.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class SyncThread extends PeriodicWork {
    public long getRecurrencePeriod() {
        return TimeUnit2.DAYS.toMillis(1);
    }

    protected void doRun() {
        for( Project p : Hudson.getInstance().getProjects() )
            new Check(p).schedule();
    }

    public static SyncThread get() {
        return all().get(SyncThread.class);
    }
}
