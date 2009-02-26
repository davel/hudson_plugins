package hudson.plugins.javanet_trigger_installer;

import hudson.Plugin;
import hudson.triggers.Trigger;
import hudson.triggers.Triggers;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {

    private Worker worker;
    private SyncThread sync;

    public void start() throws Exception {
        // start a thread that talks to java.net
        worker = new Worker();
        worker.start();

        // run re-synchronization once a day
        long HOUR = 1000*60*60;
        long DAY = HOUR*24;
        sync = new SyncThread();
        Trigger.timer.scheduleAtFixedRate(sync,DAY,DAY);
    }

    public void stop() throws Exception {
        if(worker!=null)
            worker.interrupt();
        worker = null;
    }

//
// web methods
//
    /**
     * Runs the synchronizer now.
     */
    public void doSyncNow(StaplerResponse rsp) throws IOException {
        sync.run();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("started");
    }
}
