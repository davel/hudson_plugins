package hudson.plugins.screenshot;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

public class ScreenshotBuildWrapper extends BuildWrapper {

	@DataBoundConstructor
	public ScreenshotBuildWrapper() {
	}
	
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {

		final ScreenshotAction action = new ScreenshotAction(launcher
				.getChannel());
		build.addAction(action);

		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener)
					throws IOException, InterruptedException {
				build.getActions().remove(action);
				return true;
			}
		};

	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		@Override
		public String getDisplayName() {
			return "Show screenshot during build";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

	}
}
