package hudson.plugins.vmware.labmgr;

import hudson.model.Computer;
import hudson.model.Slave;
import hudson.model.Descriptor.FormException;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.RetentionStrategy;


public class LabManagerSlaveConfiguration extends Slave {
  public static final NodeDescriptor DESCRIPTOR = new LabMgrDescriptor();

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public LabManagerSlaveConfiguration(String name, String description, String remoteFS, int numExecutors, Mode mode,
      String label, ComputerLauncher launcher, RetentionStrategy<Computer> retentionStrategy) throws FormException {
    super(name, description, remoteFS, numExecutors, mode, label, launcher, retentionStrategy);
    // TODO Auto-generated constructor stub
  }

  public NodeDescriptor getDescriptor() {
    return DESCRIPTOR;
  }

}
