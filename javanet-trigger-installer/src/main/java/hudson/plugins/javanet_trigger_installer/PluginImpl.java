package hudson.plugins.javanet_trigger_installer;

import hudson.Plugin;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {

    private Worker worker;

    public void start() throws Exception {
        // start a thread that talks to java.net
        worker = new Worker();
        worker.start();
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
        SyncThread sync = SyncThread.get();
        sync.run();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("started");
    }
}
