package hudson.plugins.vmware.labmgr;

import java.io.IOException;
import java.io.PrintStream;

import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;

public class VMLabMgrComputerLauncher extends ComputerLauncher {
  @Override
  public void launch(SlaveComputer _computer, TaskListener listener) throws IOException, InterruptedException {
    VMLabMgrComputer computer = (VMLabMgrComputer) _computer;
    PrintStream logger = listener.getLogger();
  }
}
