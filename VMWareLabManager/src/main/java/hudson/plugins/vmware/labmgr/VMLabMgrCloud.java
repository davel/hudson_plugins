package hudson.plugins.vmware.labmgr;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner.PlannedNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VMLabMgrCloud extends Cloud {
  @Extension
  public static final class DescriptorImpl extends Descriptor<Cloud>{
    @Override
    public String getDisplayName() {
      return "VMWare Labmanager";
    }
  }
  
  private List<VirtualConfigruation> configurations;

  protected VMLabMgrCloud(String name) {
    super(name);
  }

  @Override
  public boolean canProvision(Label label) {
    return getConfiguration(label) != null;
  }

  private VirtualConfigruation getConfiguration(Label label) {
    for (VirtualConfigruation conf : this.configurations) {
      if (conf.getLabel().equals(label))
        return conf;
    }
    return null;
  }

  @Override
  public Collection<PlannedNode> provision(Label label, int excessWorkload) {
    final VirtualConfigruation t = getConfiguration(label);
    
    List<PlannedNode> r = new ArrayList<PlannedNode>();
    // TODO: create a new node if necessary
    return r;
  }

}
