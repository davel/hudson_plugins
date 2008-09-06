package hudson.plugins.codeplex;

import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

public class CodePlexUserProperty extends UserProperty {

    @Override
    public UserPropertyDescriptor getDescriptor() {
        return PluginImpl.USER_PROPERTY_DESCRIPTOR;
    }

    public static class DescriptorImpl extends UserPropertyDescriptor {

        protected DescriptorImpl() {
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
