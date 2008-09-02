package hudson.plugins.rotatews;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.tasks.Publisher;

public class Rotate extends Publisher {
	@Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		FilePath ws = build.getParent().getWorkspace();
		String name = ws.getName();
		FilePath target = ws.getParent().child(name + "-" + build.getNumber());
		ws.renameTo(target);
		build.addAction(new WorkspaceBrowser(build, target));
		return super.perform(build, launcher, listener);
	}

	public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();
	
	private static class DescriptorImpl extends Descriptor<Publisher> {

		protected DescriptorImpl() {
			super(Rotate.class);
		}

		@Override
		public String getDisplayName() {
			return "Workspace Rotation";
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return new Rotate();
		}
		
	}
	
	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

}