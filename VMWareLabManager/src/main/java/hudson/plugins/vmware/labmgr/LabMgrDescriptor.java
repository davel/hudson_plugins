package hudson.plugins.vmware.labmgr;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Node;
import hudson.slaves.NodeDescriptor;

public final class LabMgrDescriptor extends NodeDescriptor {
  private String hostname;
  private String username;
  private String password;
  
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

  public LabMgrDescriptor() {
    super(LabManagerSlaveConfiguration.class);
    load();
  }
  
  @Override
  public String getDisplayName() {
    return "VMWare LabManager Plugin";
  }

  @Override
  public Node newInstance(StaplerRequest arg0, JSONObject arg1) throws hudson.model.Descriptor.FormException {
    // TODO Auto-generated method stub
    return super.newInstance(arg0, arg1);
  }
  
  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
    hostname = json.getString("hostname");
    username = json.getString("username");
    password = json.getString("password");
    save();
    return super.configure(req, json);
  }
}
