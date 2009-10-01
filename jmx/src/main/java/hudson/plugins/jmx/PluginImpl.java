package hudson.plugins.jmx;

import hudson.Plugin;
import hudson.model.listeners.ItemListener;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the plugin. This is responsible for registering listeners
 * with Hudson and create/find the MBeanServer.
 *
 * @author Renaud Bruyeron
 * @version $Id$
 * @plugin
 */
public class PluginImpl extends Plugin {
    public static final int JMX_PORT = 9876;
    /*package*/ static MBeanServer SERVER = getJMXConnectorServer();

    /**
     * @see hudson.Plugin#stop()
     */
    @Override
    public void stop() throws Exception {
        JmxJobListener jjl = ItemListener.all().get(JmxJobListener.class);
        if (jjl != null) {
            jjl.unregister();
        }
    }

    private static MBeanServer getJMXConnectorServer() {
        MBeanServer server = getPlatformMBeanServer();
        try {
            LocateRegistry.createRegistry(JMX_PORT);
            JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://" + getHostName() + ":" + JMX_PORT + "/hudson");
            JMXConnectorServer connectorServer =  JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);

            connectorServer.start();
            return connectorServer.getMBeanServer();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to start RMI connector for JMX", ex);
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, "Failed to start RMI connector for JMX", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to start RMI connector for JMX", ex);
        }
        // fall back
        return server;
    }

    private static String getHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    private static final Logger LOGGER = Logger.getLogger(PluginImpl.class.getName());

}
