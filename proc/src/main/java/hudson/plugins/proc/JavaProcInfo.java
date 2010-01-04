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
import java.util.Set;
import java.util.Properties;

import hudson.util.ProcessTree.OSProcess;

/**
 * @author Jitendra Kotamraju
 */
public class JavaProcInfo {
    private OSProcess proc;

    JavaProcInfo(OSProcess proc) {
        this.proc = proc;
    }

    String jstack() {
        StringBuilder strBuilder = new StringBuilder();
        try {
            VirtualMachine vm = VirtualMachine.attach(""+proc.getPid());

            Properties props = vm.getSystemProperties();
            strBuilder.append(props);

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
            for(ObjectName name : mbeans) {
                ThreadMXBean threadBean = ManagementFactory.newPlatformMXBeanProxy(
                        mbsc, name.toString(), ThreadMXBean.class);
                long threadIds[] = threadBean.getAllThreadIds();
                for(long threadId : threadIds) {
                    ThreadInfo threadInfo = threadBean.getThreadInfo(threadId, Integer.MAX_VALUE);
                    for(StackTraceElement elem : threadInfo.getStackTrace()) {
                        strBuilder.append(elem.toString());
                        strBuilder.append("\n");
                    }
                    strBuilder.append("\n");
                }
            }
        } catch (Exception e) {
            return e.toString();
        }
        return strBuilder.toString();
    }

}
