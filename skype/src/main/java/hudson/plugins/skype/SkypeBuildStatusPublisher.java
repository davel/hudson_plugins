package hudson.plugins.skype;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Skype Build Status
 * 
 * @author udagawa
 */
public class SkypeBuildStatusPublisher extends Publisher {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(SkypeBuildStatusPublisher.class.getName());

	private SkypeBuildStatusPublisher() {
	}

	@Override
	public boolean prebuild(final AbstractBuild<?, ?> build,
			final BuildListener listener) {
		final SkypeHelper helper = new SkypeHelper();
		final SkypeConfig config = helper.createBinding(build, null, listener);
		helper.executeScript(SCRIPT_NAME_DEFAULT, config);
		return true;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {
		final SkypeHelper helper = new SkypeHelper();
		final SkypeConfig config = helper.createBinding(build, launcher,
				listener);
		helper.executeScript(SCRIPT_NAME_DEFAULT, config);
		return true;
	}

	private static final String SCRIPT_NAME_DEFAULT = "skypeBuildStatus.groovy";

	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	/**
	 * Descriptor should be singleton.
	 */
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends Descriptor<Publisher> {

		private final String DISPLAY_NAME = "Skype Build Status";

		public DescriptorImpl() {
			super(SkypeBuildStatusPublisher.class);
			load();
		}

		@Override
		public String getDisplayName() {
			return DISPLAY_NAME;
		}

		@Override
		public String getHelpFile() {
			return "/plugin/skype/help/skype.html";
		}

		@Override
		public SkypeBuildStatusPublisher newInstance(final StaplerRequest req)
				throws FormException {
			return new SkypeBuildStatusPublisher();
		}

	}
}
