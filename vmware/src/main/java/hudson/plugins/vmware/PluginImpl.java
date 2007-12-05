package hudson.plugins.vmware;

import hudson.Plugin;
import hudson.tasks.BuildWrappers;
import hudson.util.FormFieldValidator;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Entry point of vmware plugin.
 *
 * @author Stephen Connolly
 * @plugin
 */
public class PluginImpl extends Plugin {
    private static final ConcurrentMap<String, String> vmIPAddresses = new ConcurrentHashMap<String, String>();
    private static final ConcurrentMap<String, CountDownLatch> nameLatches = new ConcurrentHashMap<String, CountDownLatch>();
    private final String URL_PREFIX = "file:/";

    public void start() throws Exception {
        BuildWrappers.WRAPPERS.add(VMwareActivationWrapper.DESCRIPTOR);
    }

    /**
     * Checks if the VIX path is a valid VIX path.
     */
    public void doVixLibraryCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        // this can be used to check the existence of a file on the server, so needs to be protected
        new FormFieldValidator(req, rsp, true) {
            public void check() throws IOException, ServletException {
                File f = getFileParameter("value");
                if (!f.isDirectory()) {
                    error(f + " is not a directory");
                    return;
                }

                File winDll = new File(f, "vix.dll");
                File linuxSO = new File(f, "libvix.so");
                if (!winDll.exists() && !linuxSO.exists()) {
                    error(f + " doesn't look like a VIX library directory");
                    return;
                }

                ok();
            }
        }.process();
    }

    /**
     * Gets the current name-value pairs of virtual machine names and IP addresses.
     *
     * @return The name-value pairs.
     */
    public Map<String, String> getVmIPAddresses() {
        return Collections.unmodifiableMap(vmIPAddresses);
    }

    /**
     * Stapler handler for setting a VM IP.
     *
     * @param req The request.
     * @param rsp The response.
     * @throws IOException If there are problems with IO.
     */
    public void doSet(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Writer w = rsp.getCompressedWriter(req);
        String key = req.getParameter("name");
        String ip1 = req.getParameter("override");
        String ip2 = req.getRemoteAddr();
        String ip = ip1 == null ? ip2 : ip1;
        if (key == null) {
            w.append("Must provide the 'name' parameter.\n");
            w.append("If the request is being forwarded through a proxy, the IP address to use can be set using the 'override' parameter.\n");
        } else {
            w.append(key + "=" + ip + "\n");
            setVMIP(key, ip);
        }
        w.append("Request originated from " + ip2 + ".");
        w.close();
    }

    /**
     * Stapler handler for unsetting a VM IP.
     *
     * @param req The request.
     * @param rsp The response.
     * @throws IOException If there are problems with IO.
     */
    public void doUnset(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Writer w = rsp.getCompressedWriter(req);
        String key = req.getParameter("name");
        if (key == null) {
            w.append("Must provide the 'name' parameter.\n");
        } else {
            w.append(key + " cleared.\n");
            clearVMIP(key);
        }
        w.append("Request originated from " + req.getRemoteAddr() + ".");
        w.close();
    }

    /**
     * Stapler handler for querying a VM IP.
     *
     * @param req The request.
     * @param rsp The response.
     * @throws IOException If there are problems with IO.
     */
    public void doQuery(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Writer w = rsp.getCompressedWriter(req);
        String key = req.getParameter("name");
        if (key == null) {
            w.append("Must provide the 'name' parameter.\n");
        } else {
            w.append(getVMIP(key));
        }
        w.close();
    }

    /**
     * Waits until the specified key has been set. Will return immediately if the key is already set.
     *
     * @param key The key to look for.
     * @throws InterruptedException If interrupted.
     */
    public static void awaitVMIP(String key) throws InterruptedException {
        if (vmIPAddresses.containsKey(key)) {
            return;
        }
        watchVMIP(key);
        final CountDownLatch latch = nameLatches.get(key);
        assert latch != null;
        latch.await();
    }

    /**
     * Waits at most <code>timeout</code> for the specified key to be set.  Will return immediately if the key is
     * already set.
     *
     * @param key     The key to look for.
     * @param timeout The timeout.
     * @param unit    The units of the timeout.
     * @return <code>true</code> if the key has been set.
     * @throws InterruptedException If interrupted.
     */
    public static boolean awaitVMIP(String key, long timeout, TimeUnit unit) throws InterruptedException {
        if (vmIPAddresses.containsKey(key)) {
            return true;
        }
        watchVMIP(key);
        final CountDownLatch latch = nameLatches.get(key);
        assert latch != null;
        return latch.await(timeout, unit);
    }

    public static void watchVMIP(String key) {
        if (!nameLatches.containsKey(key)) {
            nameLatches.putIfAbsent(key, new CountDownLatch(1));
        }
    }

    /**
     * Sets the key, releasing any threads that were waiting for it to be set.
     *
     * @param key The name.
     * @param ip  The value.
     */
    public static void setVMIP(String key, String ip) {
        vmIPAddresses.put(key, ip);
        final CountDownLatch latch = nameLatches.get(key);
        if (latch != null) {
            latch.countDown();
            nameLatches.remove(key, latch);
        }
    }

    /**
     * Clears the key.
     *
     * @param key The name.
     */
    public static void clearVMIP(String key) {
        vmIPAddresses.remove(key);
    }

    /**
     * Returns the current value of the key.
     *
     * @param key The key.
     * @return The current value or <code>null</code> if empty.
     */
    public static String getVMIP(String key) {
        return vmIPAddresses.get(key);
    }

    /**
     * Gets a set of all the current names.
     *
     * @return all the current names.
     */
    public static Set<String> getVMs() {
        return Collections.unmodifiableSet(vmIPAddresses.keySet());
    }

    private static final java.util.logging.Logger LOGGER = Logger.getLogger(PluginImpl.class.getName());
}
