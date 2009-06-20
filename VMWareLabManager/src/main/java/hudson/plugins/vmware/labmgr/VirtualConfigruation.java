package hudson.plugins.vmware.labmgr;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

public class VirtualConfigruation implements Describable<VirtualConfigruation> {
  @Extension
  public static final class DescriptorImpl extends Descriptor<VirtualConfigruation> {
    public String getDisplayName() {
      return "VMWare Labmanager Virtual Sytsem Configuration";
    }
  }
  
  private String label;

  @DataBoundConstructor
  public VirtualConfigruation(String label) {
    this.label = label;
  }

  @Override
  public Descriptor<VirtualConfigruation> getDescriptor() {
    return Hudson.getInstance().getDescriptor(getClass());
  }

  public Object getLabel() {
    return this.label;
  }

}
