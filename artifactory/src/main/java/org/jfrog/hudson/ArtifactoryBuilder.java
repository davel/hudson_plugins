package org.jfrog.hudson;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * @author Yossi Shaul
 */
public class ArtifactoryBuilder extends Builder {

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // this is where you 'build' the project
        List<ArtifactoryServer> servers = getDescriptor().getArtifactoryServers();
        if (servers.isEmpty()) {
            listener.getLogger().println("No Artifactory server configured");
        } else {
            listener.getLogger().println(servers.size() + " Artifactory servers configured");
        }
        return true;
    }

    // overrided for better type safety.
    // if your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link ArtifactoryBuilder}. Used as a singleton. The class is marked as public so that it can be
     * accessed from views.
     * <p/>
     * <p/>
     * See <tt>views/hudson/plugins/hello_world/ArtifactoryBuilder/*.jelly</tt> for the actual HTML fragment for the
     * configuration screen.
     */
    @Extension
    // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private List<ArtifactoryServer> artifactoryServers;

        public DescriptorImpl() {
            super(ArtifactoryBuilder.class);
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a name");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Artifactory Plugin";
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            artifactoryServers = req.bindParametersToList(ArtifactoryServer.class, "artifactory.");
            save();
            return super.configure(req, o);
        }

        public List<ArtifactoryServer> getArtifactoryServers() {
            return artifactoryServers;
        }
    }
}