package hudson.plugins.vmware.labmgr;

import java.text.MessageFormat;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.slaves.RetentionStrategy;
import hudson.util.TimeUnit2;

public class VMLabMgrRetentionStrategy extends RetentionStrategy<VMLabMgrComputer> {
  private static final Logger LOGGER = Logger.getLogger(VMLabMgrRetentionStrategy.class.getName());
  
  @Extension
  public static final class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
    @Override
    public String getDisplayName() {
      return "VMWare Labmanager";
    }
  }
  
  
  @Override
  public synchronized long check(VMLabMgrComputer computer) {
    if (computer.isIdle()) {
      final long idleTime = System.currentTimeMillis() - computer.getIdleStartMilliseconds();
      if (idleTime > TimeUnit2.MINUTES.toMillis(1)) {
        LOGGER.info(MessageFormat.format("Disconnecting from {0}", computer.getName()));
        computer.getNode().terminate();
      }
    }
    return 1l;
  }
  
  @Override
  public void start(VMLabMgrComputer computer) {
    computer.connect(false);
  }

}
