package hudson.plugins.emotional_hudson;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class EmotionalHudsonPublisher extends Notifier {

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
/*
        // ここはテスト用
        DESCRIPTOR.counter++;
        int value = DESCRIPTOR.counter % 5;
        switch (value) {
            case 0:
                build.setResult(Result.FAILURE);
                break;
            case 1:
                build.setResult(Result.UNSTABLE);
                break;
            case 2:
                build.setResult(Result.ABORTED);
                break;
            case 3:
                build.setResult(Result.NOT_BUILT);
                break;
            default:
        }
*/
        build.getActions().add(new EmotionalHudsonAction(build.getResult()));
        return true;
    }

    public Action getProjectAction(Project project) {
        return new EmotionalHudsonAction();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
//        public static int counter = 0;

        DescriptorImpl() {
            super(EmotionalHudsonPublisher.class);
            load();
        }

        public String getDisplayName() {
            return "Emotional Hudson";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new EmotionalHudsonPublisher();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
