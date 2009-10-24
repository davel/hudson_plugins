package hudson.plugins.buggame;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

/**
 * Descriptor for the {@link UserScoreProperty}.
 * 
 * @author Erik Ramfelt
 */
@Extension
public class UserScorePropertyDescriptor extends UserPropertyDescriptor {

    public UserScorePropertyDescriptor() {
        super(UserScoreProperty.class);
    }

    @Override
    public String getDisplayName() {
        return "Continuous Integration game";
    }
    
    @Override
    public UserScoreProperty newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
        if (formData.has("score")) {
            return req.bindJSON(UserScoreProperty.class, formData);
        } else {
            return new UserScoreProperty();
        }
    }

    @Override
    public UserProperty newInstance(User arg0) {
        return null;
    }
}
