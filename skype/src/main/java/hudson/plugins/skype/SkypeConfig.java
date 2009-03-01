package hudson.plugins.skype;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * Skype Config
 * 
 * @author udagawa
 */
public class SkypeConfig {
	public static String DEFAULT_TEST_FILE_PATTERN = "**/*.xml";
	public static String DEFAULT_ERROR_STRING_PATTERN = "BUILD (FAILURE|FAILED)";
	public static String DEFAULT_UNITTEST_PATTERN = ".*unit";

	public AbstractBuild<?, ?> build;
	public Launcher launcher;
	public BuildListener listner;
	public String chatName;
	public String testFilePattern = DEFAULT_TEST_FILE_PATTERN;
	public String errorStringPattern = DEFAULT_ERROR_STRING_PATTERN;
	public String unitTestPattern = DEFAULT_UNITTEST_PATTERN;
	public int errorLineSize = 12;
}
