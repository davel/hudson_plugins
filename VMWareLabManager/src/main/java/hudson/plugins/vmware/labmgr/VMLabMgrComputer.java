package hudson.plugins.vmware.labmgr;

import hudson.slaves.SlaveComputer;

public class VMLabMgrComputer extends SlaveComputer {

  public VMLabMgrComputer(VMLabMgrSlaveConfiguration slave) {
    super(slave);
  }
  
  @Override
  public VMLabMgrSlaveConfiguration getNode() {
    return (VMLabMgrSlaveConfiguration) super.getNode();
  }

  public VMState getState() {
    return VMState.RUNNING; // FIXME: use service call to findout in which state the vm is
  }
}
