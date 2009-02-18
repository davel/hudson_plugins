package hudson.plugins.vmware.labmgr;

import hudson.Plugin;
import hudson.slaves.NodeDescriptor;

public class LabMgrPlugin extends Plugin {
  @Override
  public void start() throws Exception {
    NodeDescriptor.ALL.add(LabManagerSlaveConfiguration.DESCRIPTOR);
  }
}
