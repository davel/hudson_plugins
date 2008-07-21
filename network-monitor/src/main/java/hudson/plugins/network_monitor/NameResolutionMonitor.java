package hudson.plugins.network_monitor;

import hudson.model.Computer;
import hudson.node_monitors.AbstractNodeMonitorDescriptor;
import hudson.node_monitors.NodeMonitor;
import hudson.remoting.Callable;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Arrays;

/**
 * @author Kohsuke Kawaguchi
 */
public class NameResolutionMonitor extends NodeMonitor {
    @DataBoundConstructor
    public NameResolutionMonitor() {
    }

    public AbstractNodeMonitorDescriptor<String> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final AbstractNodeMonitorDescriptor<String> DESCRIPTOR = new AbstractNodeMonitorDescriptor<String>(NameResolutionMonitor.class) {
        protected String monitor(Computer c) throws IOException, InterruptedException {
            // TODO: define UI
            return c.getChannel().call(new MonitorTask(Arrays.asList("www.sun.com","www.google.com","kohsuke.sfbay")));
        }

        public String getDisplayName() {
            return "nslookup";
        }
    };

    /**
     * Checks if the given host names resolve.
     */
    private static class MonitorTask implements Callable<String,RuntimeException> {
        /**
         * Host names to resolve.
         */
        private final List<String> names;

        private MonitorTask(List<String> names) {
            this.names = names;
        }

        public String call() {
            for (String name : names)
                try {
                    InetAddress.getByName(name);
                } catch (UnknownHostException e) {
                    return name;
                }
            return null;    // all green ... I mean, all blue!
        }

        private static final long serialVersionUID = 1L;
    }

}
