package hudson.plugins.skype;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Skype Build Error
 * 
 * @author udagawa
 */
public class SkypeBuildErrorPublisher extends Publisher {

	private static final Logger LOGGER = Logger
			.getLogger(SkypeBuildErrorPublisher.class.getName());

	private SkypeBuildErrorPublisher(final String chatName,
			final String errorStringPattern) {
		_chatName = chatName;
		_errorStringPattern = errorStringPattern;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {
		if (StringUtils.isBlank(getChatName())) {
			LOGGER.log(Level.WARNING, "you must specify chat name.");
			return false;
		}
		final SkypeHelper helper = new SkypeHelper();
		final SkypeConfig config = helper.createBinding(build, launcher,
				listener);
		config.chatName = getChatName();
		config.errorStringPattern = getErrorStringPattern();
		helper.executeScript(SCRIPT_NAME_DEFAULT, config);
		return true;
	}

	private static final String SCRIPT_NAME_DEFAULT = "skypeBuildError.groovy";

	private final String _chatName;

	private String _errorStringPattern;

	public String getChatName() {
		return _chatName;
	}

	public String getErrorStringPattern() {
		if (StringUtils.isBlank(_errorStringPattern)) {
			_errorStringPattern = SkypeConfig.DEFAULT_ERROR_STRING_PATTERN;
		}
		return _errorStringPattern;
	}

	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	/**
	 * Descriptor should be singleton.
	 */
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends Descriptor<Publisher> {

		private final String DISPLAY_NAME = "Skype Build Error";
		private final String PARAMETER_CHAT_NAME = "skype.chatNameBuildError";
		private final String PARAMETER_ERROR_STRING_PATTERN = "skype.errorStringPattern";

		public DescriptorImpl() {
			super(SkypeBuildErrorPublisher.class);
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

		public String getDefaultErrorStringPattern() {
			return SkypeConfig.DEFAULT_ERROR_STRING_PATTERN;
		}

		@Override
		public SkypeBuildErrorPublisher newInstance(final StaplerRequest req)
				throws FormException {
			return new SkypeBuildErrorPublisher(req
					.getParameter(PARAMETER_CHAT_NAME), req
					.getParameter(PARAMETER_ERROR_STRING_PATTERN));
		}
	}
}
