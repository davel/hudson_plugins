package hudson.plugins.codeplex;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

public class CodePlexUserProperty extends UserProperty {

    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {

        public DescriptorImpl() {
            super(CodePlexUserProperty.class);
        }

        @Override
        public UserProperty newInstance(User arg0) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "CodePlex Home";
        }
        
    }
}
