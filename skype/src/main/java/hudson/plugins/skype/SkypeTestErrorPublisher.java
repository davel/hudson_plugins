package hudson.plugins.skype;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Skype Test Error
 * 
 * @author udagawa
 */
public class SkypeTestErrorPublisher extends Notifier {

	private static final Logger LOGGER = Logger
			.getLogger(SkypeTestErrorPublisher.class.getName());

	private SkypeTestErrorPublisher(final String chatName,
			final String unitTestPattern,
			final String testFilePattern) {
		_chatName = chatName;
		_unitTestPattern = unitTestPattern;
		_testFilePattern = testFilePattern;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {
		if (StringUtils.isBlank(getChatName())) {
			LOGGER.log(Level.WARNING, "you must specify chat name.");
			return true;
		}
		final SkypeHelper helper = new SkypeHelper();
		final SkypeConfig config = helper.createBinding(build, launcher,
				listener);
		config.chatName = getChatName();
		config.unitTestPattern = getUnitTestPattern();
		config.testFilePattern = getTestFilePattern();
		helper.executeScript(SCRIPT_NAME_DEFAULT, config);
		return true;
	}

	private static final String SCRIPT_NAME_DEFAULT = "skypeTestError.groovy";

	private final String _chatName;

	public String getChatName() {
		return _chatName;
	}

	private String _unitTestPattern;

	public String getUnitTestPattern() {
		if (StringUtils.isBlank(_unitTestPattern)) {
			_unitTestPattern = SkypeConfig.DEFAULT_UNITTEST_PATTERN;
		}
		return _unitTestPattern;
	}

	private String _testFilePattern;

	public String getTestFilePattern() {
		if (StringUtils.isBlank(_testFilePattern)) {
			_testFilePattern = SkypeConfig.DEFAULT_TEST_FILE_PATTERN;
		}
		return _testFilePattern;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		private final String DISPLAY_NAME = "Skype Test Error";
		private final String PARAMETER_CHAT_NAME = "skype.chatNameTestError";
		private final String PARAMETER_UNITTEST_PATTERN = "skype.unitTestPattern";
		private final String PARAMETER_TEST_FILE_PATTERN = "skype.testFilePattern";

		public DescriptorImpl() {
			super(SkypeTestErrorPublisher.class);
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

		public String getDefaultUnitTestPattern() {
			return SkypeConfig.DEFAULT_UNITTEST_PATTERN;
		}

		public String getDefaultTestFilePattern() {
			return SkypeConfig.DEFAULT_TEST_FILE_PATTERN;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public SkypeTestErrorPublisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return new SkypeTestErrorPublisher(
				req.getParameter(PARAMETER_CHAT_NAME), 
				req.getParameter(PARAMETER_UNITTEST_PATTERN),
				req.getParameter(PARAMETER_TEST_FILE_PATTERN)
			);
		}
	}
}
