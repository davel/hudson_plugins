package de.fspengler.hudson.pview;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.views.ListViewColumn;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class ConsoleViewColumn extends ListViewColumn {

    public Descriptor<ListViewColumn> getDescriptor() {
        return DESCRIPTOR;
    }
    
    public static final Descriptor<ListViewColumn> DESCRIPTOR = new DescriptorImpl();

    @Extension
    public static class DescriptorImpl extends Descriptor<ListViewColumn> {
        @Override
        public ListViewColumn newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // This will be called with req == null also the Descriptor's doc tells you not. so the default impl fails
            return new ConsoleViewColumn();
        }

        @Override
        public String getDisplayName() {
            return "Console";
        }
    }
}
