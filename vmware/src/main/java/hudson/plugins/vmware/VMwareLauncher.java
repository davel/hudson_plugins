package hudson.plugins.vmware;

import hudson.slaves.SlaveComputer;
import hudson.slaves.ComputerLauncher;
import hudson.util.StreamTaskListener;
import hudson.model.Descriptor;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 20-May-2008 21:48:04
 */
public class VMwareLauncher extends ComputerLauncher {
    private final VMwareVMConfig virtualMachine;

    @DataBoundConstructor
    public VMwareLauncher(VMwareVMConfig virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    public void launch(SlaveComputer slaveComputer, StreamTaskListener streamTaskListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public VMwareVMConfig getVirtualMachine() {
        return virtualMachine;
    }

    public Descriptor<ComputerLauncher> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final Descriptor<ComputerLauncher> DESCRIPTOR = new DescriptorImpl();

    private static class DescriptorImpl extends Descriptor<ComputerLauncher> {
        protected DescriptorImpl() {
            super(VMwareLauncher.class);
        }

        public String getDisplayName() {
            return "Launch a VMware virtual machine based slave";
        }
        public List<VMwareHostConfig> getHosts() {
            return VMwareActivationWrapper.DESCRIPTOR.getHosts();
        }

    }
}
