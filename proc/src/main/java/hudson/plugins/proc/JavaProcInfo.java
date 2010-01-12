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
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
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

    // Keep it static inner class, otherwise JavaProcInfo needs to be
    // specified as Serializable
    private static class PropertiesTask implements Callable<Properties, Exception> {
        private final String pid;

        PropertiesTask(String pid) {
            this.pid = pid;
        }

        public Properties call() throws Exception {
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

    // returns the java stack for the process
    public List<String> jstack() throws Exception {
        return run.getBuiltOn().getChannel().call(new JstackTask(""+proc.getPid()));
    }

    // Keep it static inner class, otherwise JavaProcInfo needs to be
    // specified as Serializable
    private static class JstackTask implements Callable<List<String>, Exception> {
        private final String pid;

        JstackTask(String pid) {
            this.pid = pid;
        }

        public List<String> call() throws Exception {
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

}
