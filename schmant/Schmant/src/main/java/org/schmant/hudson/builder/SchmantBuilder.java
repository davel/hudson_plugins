package org.schmant.hudson.builder;

import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public final class SchmantBuilder extends Builder
{
	public static final SchmantBuilderDescriptor DESCRIPTOR = new SchmantBuilderDescriptor();

	// The JAVA_HOME environment variable
	private static final String JAVA_HOME = "JAVA_HOME";
	private static final String PATH_SEPARATOR = System.getProperty("path.separator");
	private static final String SCHMANT_LAUNCHER_CLASS = "org.schmant.Launcher";

	private final String m_scriptFile;
	private final String m_systemProperties;
	private final String m_variables;
	private final String m_scriptArguments;
	private final String m_taskPackagePath;
	private final String m_scriptEngineName;
	private final String m_verbosity;
	// Additional classpath, for loading script engine jars, etc.
	private final String m_additionalClasspath;

	@DataBoundConstructor
	public SchmantBuilder(String scriptFile, String variables, String scriptArguments, String systemProperties, String taskPackagePath, String scriptEngineName, String verbosity, String additionalClasspath)
	{
		m_scriptFile = scriptFile;
		m_systemProperties = systemProperties;
		m_variables = variables;
		m_scriptArguments = scriptArguments;
		m_taskPackagePath = taskPackagePath;
		m_scriptEngineName = scriptEngineName;
		m_verbosity = verbosity != null ? verbosity : "Info";
		m_additionalClasspath = additionalClasspath;
	}

	public String getScriptFile()
	{
		return m_scriptFile;
	}

	public String getSystemProperties()
	{
		return m_systemProperties;
	}

	public String getVariables()
	{
		return m_variables;
	}

	public String getScriptArguments()
	{
		return m_scriptArguments;
	}

	public String getTaskPackagePath()
	{
		return m_taskPackagePath;
	}

	public String getScriptEngineName()
	{
		return m_scriptEngineName;
	}

	public String getVerbosity()
	{
		return m_verbosity;
	}

	public String getAdditionalClasspath()
	{
		return m_additionalClasspath;
	}

	private boolean validateConfiguration(BuildListener listener)
	{
		boolean ok = true;
		if (DESCRIPTOR.getSchmantHome() == null)
		{
			listener.fatalError("The Schmant home directory is not configured");
			ok = false;
		}
		if (m_scriptFile == null)
		{
			listener.fatalError("No script file set");
			ok = false;
		}
		return ok;
	}

	private String getJavaCommand(Build<?, ?> build, TaskListener log) throws IOException, InterruptedException
	{
		String javaHome = build.getEnvironment(log).get(JAVA_HOME);
		if (javaHome == null)
		{
			throw new RuntimeException("The " + JAVA_HOME + " environment variable is not set. Check the Hudson configuration");
		}
		File javaHomef = new File(javaHome);
		File javaf = new File(javaHomef, "bin/java");
		if (javaf.exists() && javaf.isFile())
		{
			return javaf.getAbsolutePath();
		}

		File javaexef = new File(javaHomef, "bin/java.exe");
		if (!javaexef.exists() && javaexef.isFile())
		{
			throw new RuntimeException("Neither of the " + javaf + " or " + javaexef + " files exist. Check Hudson's JAVA_HOME configuration");
		}
		return javaexef.getAbsolutePath();
	}

	private String createClasspath(File schmantHome)
	{
		StringBuilder sb = new StringBuilder();
		if (m_additionalClasspath != null)
		{
			sb.append(m_additionalClasspath).append(PATH_SEPARATOR);
		}

		// Add all Jar files in schmantHome/lib to the classpath
		// Assume that schmantHome is an absolute path
		File lib = new File(schmantHome, "lib");
		File[] jarFiles = lib.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(".jar");
			}
		});

		for (File jarFile : jarFiles)
		{
			sb.append(jarFile.getPath()).append(PATH_SEPARATOR);
		}
		return sb.toString();
	}

	private Map<String, String> getProperties(String text) throws IOException
	{
		Properties props = new Properties();
		if (text != null)
		{
			props.load(new StringReader(text));
		}
		HashMap<String, String> res = new HashMap<String, String>(props.size());
		for (Map.Entry<Object, Object> prop : props.entrySet())
		{
			res.put((String) prop.getKey(), (String) prop.getValue());
		}
		return res;
	}

	private void addVerbosity(ArgumentListBuilder al)
	{
		if ("Warnings".equals(m_verbosity))
		{
			al.add("-q");
		}
		else if ("Errors".equals(m_verbosity))
		{
			al.add("-q").add("-q");
		}
		else if ("Debug".equals(m_verbosity))
		{
			al.add("-v");
		}
		else if ("Trace".equals(m_verbosity))
		{
			al.add("-v").add("-v");
		}
		else if ("Info".equals(m_verbosity))
		{
			// Ignore
		}
		else
		{
			throw new RuntimeException("Invalid verbosity level: " + m_verbosity);
		}
	}

	private Map<String, String> createVariables(Build<?, ?> build)
	{
		Map<String, String> res = new HashMap<String, String>();
		res.put("buildNumber", "" + build.getNumber());
		res.put("buildId", build.getId());
		res.put("jobName", build.getParent().getName());
		res.put("buildTag", "hudson-" + build.getParent().getName() + "-" + build.getNumber());
		res.put("executorNumber", Integer.toString(build.getExecutor().getNumber()));
		res.put("workspace", build.getWorkspace().getRemote());
		return res;
	}

	private ArgumentListBuilder createArgumentListBuilder(Build<?, ?> build, BuildListener listener, File schmantHome) throws IOException, InterruptedException
	{
		ArgumentListBuilder res = new ArgumentListBuilder();
		res.add(getJavaCommand(build, listener));
		res.add("-cp", createClasspath(schmantHome));

		if (m_systemProperties != null)
		{
			res.addKeyValuePairs("-D", getProperties(m_systemProperties));
		}

		res.add(SCHMANT_LAUNCHER_CLASS);

		res.add("-sh", schmantHome.getPath());

		if ((m_taskPackagePath != null) && (m_taskPackagePath.length() > 0))
		{
			res.add("-t", m_taskPackagePath);
		}

		if ((m_scriptEngineName != null) && (m_scriptEngineName.length() > 0))
		{
			res.add("--script-engine", m_scriptEngineName);
		}

		if ((m_verbosity != null) && (m_verbosity.length() > 0) && !"Info".equals(m_verbosity))
		{
			addVerbosity(res);
		}

		// Create standard variables
		Map<String, String> variables = createVariables(build);
		if (m_variables != null)
		{
			// Set new variables and maybe override standard variables with
			// values set in the configuration
			variables.putAll(getProperties(m_variables));
		}
		res.addKeyValuePairs("-p", variables);

		res.add(m_scriptFile);

		if ((m_scriptArguments != null) && (m_scriptArguments.length() > 0))
		{
			res.addTokenized(m_scriptArguments);
		}
		return res;
	}

	private Launcher.ProcStarter createLauncher(Build<?, ?> build, BuildListener listener, File schmantHome) throws IOException, InterruptedException
	{
		Launcher l = build.getWorkspace().createLauncher(listener);
		Launcher.ProcStarter res = l.launch();
		res.cmds(createArgumentListBuilder(build, listener, schmantHome));
		res.stdout(listener);
		res.pwd(build.getWorkspace());
		return res;
	}

	public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException
	{
		if (!validateConfiguration(listener))
		{
			return false;
		}

		File schmantHome = new File(DESCRIPTOR.getSchmantHome());
		listener.getLogger().println("Schmant home: " + schmantHome);

		Launcher.ProcStarter ps = createLauncher(build, listener, schmantHome);
		int exitCode = ps.join();

		if (exitCode != 0)
		{
			listener.fatalError("The Schmant process exited with error code " + exitCode);
			return false;
		}
		else
		{
			return true;
		}
	}

	public Descriptor<Builder> getDescriptor()
	{
		return DESCRIPTOR;
	}

	/**
	 * Apparently, this <i>must</i> be an inner class for the configuration to
	 * work.
	 */
	public static class SchmantBuilderDescriptor extends Descriptor<Builder>
	{
		private String m_schmantHome;

		private SchmantBuilderDescriptor()
		{
			super(SchmantBuilder.class);
			load();
		}

		public FormValidation doCheckSchmantHome(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException
		{
			if (value.length() == 0)
			{
				return FormValidation.warning("Schmant home must be set");
			}
			else
			{
				File f = new File(value);
				if (!f.exists())
				{
					return FormValidation.error(f + " does not exist");
				}
				else if (!f.isDirectory())
				{
					return FormValidation.error(f + " is not a directory");
				}
				else
				{
					return FormValidation.ok();
				}
			}
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName()
		{
			return "Invoke Schmant";
		}

		public boolean configure(StaplerRequest req, JSONObject o) throws FormException
		{
			m_schmantHome = o.getString("home");
			save();
			return super.configure(req, o);
		}

		public String getSchmantHome()
		{
			return m_schmantHome;
		}
	}
}
