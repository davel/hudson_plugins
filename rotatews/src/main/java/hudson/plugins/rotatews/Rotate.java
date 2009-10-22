package hudson.plugins.rotatews;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

public class Rotate extends Notifier {
	@Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		FilePath ws = build.getWorkspace();
		String name = ws.getName();
		FilePath target = ws.getParent().child(name + "-" + build.getNumber());
		ws.renameTo(target);
		build.addAction(new WorkspaceBrowser(build, target));
		return true;
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super(Rotate.class);
		}

		@Override
		public String getDisplayName() {
			return "Workspace Rotation";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return new Rotate();
		}
	}
}