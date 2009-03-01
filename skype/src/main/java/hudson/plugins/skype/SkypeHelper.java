package hudson.plugins.skype;

import groovy.lang.Binding;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.spring.BeanBuilder;

import java.io.File;

/**
 * Skype Helper
 * 
 * @author udagawa
 */
public class SkypeHelper {

	static final String JAVA_LIBRAY_PATH = "java.library.path";
	static final String GROOVY_HOME = "GROOVY_HOME";
	static final String SWT_DLL_WIN32 = "swt-win32-3232.dll";
	static final String SKYPE_CONFIG_KEY = "skypeConfig";

	public SkypeHelper() {
		// if GROOVY_HOME is set, error occured.
		System.setProperty(GROOVY_HOME, "");
		// add dll(for Windows)
		addJavaLibrayPath(SWT_DLL_WIN32);
	}

	public String addJavaLibrayPath(final String dll) {
		String javaLibraryPath = System.getProperty(JAVA_LIBRAY_PATH);
		if (javaLibraryPath.indexOf(dll) < 0) {
			javaLibraryPath += File.pathSeparator + dll;
			System.setProperty(JAVA_LIBRAY_PATH, javaLibraryPath);
		}
		return javaLibraryPath;
	}

	public SkypeConfig createBinding(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener) {
		final SkypeConfig config = new SkypeConfig();
		config.build = build;
		config.launcher = launcher;
		config.listner = listener;
		return config;
	}

	public void executeScript(final String groovyScriptFile,
			final SkypeConfig config) {
		final BeanBuilder builder = new BeanBuilder(getClass().getClassLoader());
		final Binding binding = new Binding();
		binding.setProperty(SKYPE_CONFIG_KEY, config);
		builder
				.parse(getClass().getResourceAsStream(groovyScriptFile),
						binding);
	}
}
