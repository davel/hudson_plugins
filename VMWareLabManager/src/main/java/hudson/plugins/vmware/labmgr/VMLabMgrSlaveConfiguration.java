package hudson.plugins.vmware.labmgr;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Slave;
import hudson.model.Descriptor.FormException;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;


public class VMLabMgrSlaveConfiguration extends Slave {
  @Extension
  public static final class DescriptorImpl extends SlaveDescriptor {
    public String getDisplayName() {
      return "VMWare LabManager Slave";
    }
  }
  private static final long serialVersionUID = 1L;

  private String hostname;
  private String username;
  private String password;
  
  @DataBoundConstructor
  public VMLabMgrSlaveConfiguration(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String label, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws IOException, FormException {
    super(name, nodeDescription, remoteFS, numExecutors, mode, label, launcher, retentionStrategy, nodeProperties);
  }


  @Override
  public Computer createComputer() {
      return new VMLabMgrComputer(this);
  }
  
  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void terminate() {
    // TODO Auto-generated method stub
    
  }
  
}
