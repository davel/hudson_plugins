package hudson.plugins.sctmexecutor;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * 
 * @author Thomas Fuerer
 *
 */
public final class SCTMExecutorDescriptor extends Descriptor<Builder> {   
  private String serviceURL;
  private String user;
  private String password;

  SCTMExecutorDescriptor() {
    super(SCTMExecutor.class);
    load();
  }

  @Override
  public String getDisplayName() {
    return Messages.getString("SCTMExecutorDescriptor.plugin.title"); //$NON-NLS-1$
  }

  @Override
  public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
    String execDefIds = formData.getString("execDefId"); //$NON-NLS-1$
    String str = formData.getString("projectId"); //$NON-NLS-1$
    int projectId = Integer.parseInt(str);
    return new SCTMExecutor(projectId, execDefIds);
  }
  
  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
    serviceURL = json.getString("serviceURL"); //$NON-NLS-1$
    user = json.getString("user"); //$NON-NLS-1$
    try {
      password = PwdCrypt.encode(json.getString("password"), Hudson.getInstance().getSecretKey());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    save();
    return super.configure(req, json);
  }

  public void setServiceURL(String serviceURL) {
    this.serviceURL = serviceURL;
  }

  public String getServiceURL() {
    return serviceURL;
  }
  
  public String getUser() {
    return user;
  }

  public String getPassword() {
    try {
      return PwdCrypt.decode(password, Hudson.getInstance().getSecretKey());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}