package hudson.plugins.proc;

import com.sun.tools.attach.VirtualMachine;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;
import hudson.util.ProcessTree.OSProcess;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Gives Java process info for e.g stack, system properties
 *
 * @author Jitendra Kotamraju
 */
public class JavaProcInfo extends ProcInfo {

    JavaProcInfo(AbstractBuild run, OSProcess proc) {
        super(run, proc);
    }

    // returns the system properties for the process
    public Properties getSystemProperties() throws Exception {
        return run.getBuiltOn().getChannel().call(new PropertiesTask(""+proc.getPid()));
    }

    // returns the java stack for the process
    public List<String> jstack() throws Exception {
        return run.getBuiltOn().getChannel().call(new JstackTask(""+proc.getPid()));
    }

    // Keep it static inner class, otherwise JavaProcInfo needs to be
    // specified as Serializable
    private static class PropertiesTask extends RemoteTask<Properties> {
        PropertiesTask(String pid) {
            super(pid);
        }

        public Properties call() throws Exception {
            super.call();
            VirtualMachine vm = null;
            try {
                vm = VirtualMachine.attach(pid);
                return vm.getSystemProperties();
            } finally {
                if (vm != null) {
                    try {
                        vm.detach();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    // Keep it static inner class, otherwise JavaProcInfo needs to be
    // specified as Serializable
    private static class JstackTask extends RemoteTask<List<String>> {
        JstackTask(String pid) {
            super(pid);
        }

        public List<String> call() throws Exception {
            super.call();
            List<String> tiList = new ArrayList<String>();
            VirtualMachine vm = null;
            try {
                vm = VirtualMachine.attach(pid);

                String connectorAddr = vm.getAgentProperties().getProperty(
                        "com.sun.management.jmxremote.localConnectorAddress");
                if (connectorAddr == null) {
                    String agent = vm.getSystemProperties().getProperty(
                            "java.home") + File.separator + "lib" + File.separator +
                            "management-agent.jar";
                    vm.loadAgent(agent);
                    connectorAddr = vm.getAgentProperties().getProperty(
                            "com.sun.management.jmxremote.localConnectorAddress");
                }
                JMXServiceURL serviceURL = new JMXServiceURL(connectorAddr);
                JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
                MBeanServerConnection mbsc = connector.getMBeanServerConnection();
                ObjectName objName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
                Set<ObjectName> mbeans = mbsc.queryNames(objName, null);
                for (ObjectName name : mbeans) {
                    ThreadMXBean threadBean = ManagementFactory.newPlatformMXBeanProxy(
                            mbsc, name.toString(), ThreadMXBean.class);
                    long threadIds[] = threadBean.getAllThreadIds();
                    for (long threadId : threadIds) {
                        tiList.add(threadBean.getThreadInfo(threadId, Integer.MAX_VALUE).toString());
                    }
                }
            } finally {
                if (vm != null) {
                    try {
                        vm.detach();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            return tiList;
        }
    }

    private static class RemoteTask<T> implements Callable<T, Exception> {
        static final Exception TOOLS_EXCEPTION;
        static {
            Exception te = null;
            try {
                addToolsJar();
            } catch(Exception e) {
                te = e;
            }
            TOOLS_EXCEPTION = te;
        }
        protected final String pid;

        protected RemoteTask(String pid) {
            this.pid = pid;
        }

        public T call() throws Exception {
            if (TOOLS_EXCEPTION != null) {
                throw TOOLS_EXCEPTION;
            }
            return null;
        }

        // If the slave's JVM doesn't have tools.jar in the classpath, it is added
        // to the system class loader.
        //
        // URLClassLoader#addURL(URL) is called reflectively with tools.jar's URL
        //
        private static void addToolsJar() throws Exception {
            ClassLoader cl = hudson.remoting.Channel.class.getClassLoader();
            try {
                cl.loadClass("com.sun.tools.attach.VirtualMachine");
            } catch(ClassNotFoundException ce) {
                if (cl instanceof URLClassLoader) {
                    // Try to find tools.jar
                    File jreHome = new File(System.getProperty("java.home"));
                    File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );
                    if (!toolsJar.exists()) {
                        throw new RuntimeException("Cannot find tools.jar for this slave's JVM");
                    }
                    URL toolsURL = toolsJar.toURL();
                    Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    m.setAccessible(true);
                    m.invoke(cl, toolsURL);
                } else {
                    throw new RuntimeException("Cannot add tools.jar to Slave's system classloader");
                }
            }
        }
    }

}
