package hudson.plugins.virtualization;

import hudson.util.Secret;
import hudson.util.FormValidation;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.Extension;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.io.IOException;

import net.java.dev.vcc.api.Computer;
import net.java.dev.vcc.api.ManagedObjectId;
import net.java.dev.vcc.api.Datacenter;
import net.java.dev.vcc.api.DatacenterManager;

import javax.servlet.ServletException;

/**
 * Represents a virtual datacenter.
 */
public class VirtualDatacenter extends Cloud {

    private static final Logger LOGGER = Logger.getLogger(VirtualDatacenter.class.getName());

    private final String datacenterUri;
    private final String username;
    private final Secret password;
    private final int refreshSeconds;

    private transient Map<ManagedObjectId<Computer>, Computer> computers = null;
    private transient long nextRefresh = 0;
    private transient Thread updatingCacheThread = null;
    private transient Datacenter datacenter = null;

    @DataBoundConstructor
    public VirtualDatacenter(String datacenterUri, String username, String password, int refreshSeconds) {
        super("vcc-api");
        this.datacenterUri = datacenterUri;
        this.username = username;
        this.password = Secret.fromString(password.trim());
        this.refreshSeconds = refreshSeconds <= 0 ? 60 : refreshSeconds;
        updateComputersCache();
    }

    protected Object readResolve() {
        updateComputersCache();
        return this;
    }


    public String getDatacenterUri() {
        return datacenterUri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password.getEncryptedValue();
    }

    public int getRefreshSeconds() {
        return refreshSeconds;
    }

    private synchronized Thread updateComputersCache() {
        if (updatingCacheThread == null || !updatingCacheThread.isAlive()) {
            updatingCacheThread = new Thread() {
                @Override
                public void run() {
                    LOGGER.info("Starting cache update");
                    Map<ManagedObjectId<Computer>, Computer> computers
                            = new HashMap<ManagedObjectId<Computer>, Computer>();
                    try {
                        getConnection();
                        for (Computer c : datacenter.getAllComputers()) {
                            computers.put(c.getId(), c);
                        }
                        LOGGER.info("Saving updated cache");
                        synchronized (VirtualDatacenter.this) {
                            VirtualDatacenter.this.computers = computers;
                            nextRefresh = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(refreshSeconds);
                            updatingCacheThread = null;
                        }
                    } catch (IOException e) {
                        LogRecord rec = new LogRecord(Level.SEVERE, "Cannot connect to datacenter {0} as {1}/******");
                        rec.setThrown(e);
                        rec.setParameters(new Object[]{datacenterUri, username});
                        LOGGER.log(rec);
                    } catch (InterruptedException e) {
                        LogRecord rec = new LogRecord(Level.SEVERE, "Cannot connect to datacenter {0} as {1}/******");
                        rec.setThrown(e);
                        rec.setParameters(new Object[]{datacenterUri, username});
                        LOGGER.log(rec);
                    } finally {
                        LOGGER.info("Finished updating cache");
                    }
                }
            };
            updatingCacheThread.start();
        }
        return updatingCacheThread;
    }

    public synchronized Datacenter getConnection() throws IOException, InterruptedException {
        LOGGER.info("Checking for connection");
        if (datacenter == null || !datacenter.isOpen()) {
            LOGGER.info("Reconnect");
            datacenter = MakeConnectionThread.getConnection(datacenterUri, username, password.toString());
        }
        LOGGER.info("Have connection");
        return datacenter;
    }

    public synchronized Map<ManagedObjectId<Computer>, Computer> getComputers() {
        if (computers == null || System.currentTimeMillis() > nextRefresh) {
            updateComputersCache();
        }
        return computers == null ? new HashMap<ManagedObjectId<Computer>, Computer>() : computers;
    }

    public Collection<NodeProvisioner.PlannedNode> provision(Label label, int i) {
        return Collections.emptySet();
    }

    public boolean canProvision(Label label) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Descriptor<Cloud> getDescriptor() {
        return Hudson.getInstance().getDescriptor(getClass());
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Cloud> {
        public String getDisplayName() {
            return "Virtual Datacenter (via vcc-api)";
        }

        public FormValidation doTestConnection(
                @QueryParameter String datacenterUri, @QueryParameter String username,
                @QueryParameter String password) throws IOException, ServletException {
            try {
                if (datacenterUri == null) {
                    return FormValidation.error("Datacenter URI is not specified");
                }
                if (!datacenterUri.startsWith("vcc+")) {
                    return FormValidation.error("Datacenter URI is not a valid vcc-api URI");
                }
                if (username == null) {
                    return FormValidation.error("Username is not specified");
                }
                if (password == null) {
                    return FormValidation.error("Password is not specified");
                }
                Datacenter datacenter = MakeConnectionThread
                        .getConnection(datacenterUri, username, Secret.fromString(password).toString());
                datacenter.close();
                return FormValidation.ok("Connected successfully");
            } catch (IOException e) {
                LogRecord rec = new LogRecord(Level.WARNING,
                        "Failed to check datacenter connection to {0} as {1}/******");
                rec.setThrown(e);
                rec.setParameters(new Object[]{datacenterUri, username});
                LOGGER.log(rec);
                return FormValidation.error(e.getMessage());
            } catch (InterruptedException e) {
                LogRecord rec = new LogRecord(Level.WARNING,
                        "Failed to check datacenter connection to {0} as {1}/******");
                rec.setThrown(e);
                rec.setParameters(new Object[]{datacenterUri, username});
                LOGGER.log(rec);
                return FormValidation.error(e.getMessage());
            }
        }

    }

    static final class MakeConnectionThread extends Thread {
        private final String datacenterUri;
        private final String username;
        private final char[] password;
        private volatile Datacenter datacenter = null;
        private volatile IOException ioException = null;

        private MakeConnectionThread(String datacenterUri, String username, char[] password) {
            this.datacenterUri = datacenterUri;
            this.username = username;
            this.password = password;
        }

        public static Datacenter getConnection(String datacenterUri, String username, String password)
                throws IOException, InterruptedException {
            MakeConnectionThread t = new MakeConnectionThread(datacenterUri, username, password.toCharArray());
            t.setContextClassLoader(VirtualDatacenter.class.getClassLoader());
            t.start();
            t.join();
            if (t.datacenter != null) {
                LOGGER.log(Level.INFO, "Have connection to datacenter URI: {0} as {1}/******",
                        new Object[]{datacenterUri, username});
                return t.datacenter;
            }
            if (t.ioException == null) {
                throw new IOException("Unknown error trying to establish a connection");
            }
            throw t.ioException;
        }

        @Override
        public void run() {
            try {
                LOGGER.log(Level.INFO, "Trying to establish a connection to datacenter URI: {0} as {1}/******",
                        new Object[]{datacenterUri, username});
                datacenter = DatacenterManager.getConnection(datacenterUri, username, password);
                LOGGER.log(Level.INFO, "Established connection to datacenter URI: {0} as {1}/******",
                        new Object[]{datacenterUri, username});
            } catch (IOException e) {
                LogRecord rec = new LogRecord(Level.WARNING,
                        "Failed to establish connection to datacenter URI: {0} as {1}/******");
                rec.setThrown(e);
                rec.setParameters(new Object[]{datacenterUri, username});
                LOGGER.log(rec);
                ioException = e;
            }
        }
    }
}
