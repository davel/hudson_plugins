package hudson.plugins.vmware.labmgr;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Slave;
import hudson.model.Descriptor.FormException;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;


public class LabManagerSlaveConfiguration extends Slave {
  private static final long serialVersionUID = 1L;

  private String hostname;
  private String username;
  private String password;
  
  @DataBoundConstructor
  public LabManagerSlaveConfiguration(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String label, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws IOException, FormException {
    super(name, nodeDescription, remoteFS, numExecutors, mode, label, launcher, retentionStrategy, nodeProperties);
  }

  @Extension
  public static final class DescriptorImpl extends SlaveDescriptor {
      public String getDisplayName() {
        return "VMWare LabManager Integration";
//          return Messages.DumbSlave_displayName();
      }
  }

  @Override
  public Computer createComputer() {
    try {
      return new VMComputer(this, new URL(MessageFormat.format("https://{0}/LabManager/SOAP/LabManager.asmx", hostname)));
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    } 
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
  
}
