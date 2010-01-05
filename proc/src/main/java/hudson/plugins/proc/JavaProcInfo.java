package hudson.plugins.proc;

import com.sun.tools.attach.VirtualMachine;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import java.util.*;

import hudson.util.ProcessTree.OSProcess;

/**
 * Gives Java process info for e.g stack, system properties
 *
 * @author Jitendra Kotamraju
 */
public class JavaProcInfo extends ProcInfo {

    JavaProcInfo(OSProcess proc) {
        super(proc);
    }

    // returns the system properties for the process
    public Properties getSystemProperties() {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach("" + proc.getPid());
            return vm.getSystemProperties();
        } catch (Exception ioe) {
            return new Properties();
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

    // returns the java stack for the process
    public List<ThreadInfo> jstack() {
        List<ThreadInfo> tiList = new ArrayList<ThreadInfo>();
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach("" + proc.getPid());

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
                    tiList.add(threadBean.getThreadInfo(threadId, Integer.MAX_VALUE));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
