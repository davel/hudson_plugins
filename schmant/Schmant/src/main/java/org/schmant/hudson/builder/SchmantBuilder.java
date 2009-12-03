package org.schmant.hudson.builder;

import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.script.ScriptException;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.schmant.hudson.util.SchmantHomeClassLoader;

public final class SchmantBuilder extends Builder
{
	public static final SchmantBuilderDescriptor DESCRIPTOR = new SchmantBuilderDescriptor();

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

	private Method findLauncherMethod(Class<?> c)
	{
		for (Method m : c.getMethods())
		{
			if ("launch".equals(m.getName()))
			{
				return m;
			}
		}
		throw new RuntimeException("Did not find \"launch\" method on " + c + " class");
	}

	private Properties getProperties(String text) throws IOException
	{
		Properties res = new Properties();
		if (text != null)
		{
			res.load(new StringReader(text));
		}
		return res;
	}

	private Map<String, Object> createLauncherSettings(File schmantHome, Build<?, ?> build, BuildListener listener) throws IOException
	{
		Map<String, Object> res = new HashMap<String, Object>();
		File scriptFile = new File(m_scriptFile);
		if (!scriptFile.isAbsolute())
		{
			// Make this file relative to the workspace directory
			// Must take a detour around Object, otherwise the compiler does not
			// think that the cast will work.
			Object o = build.getParent();
			scriptFile = new File(((FreeStyleProject) o).getSomeWorkspace().getRemote() + File.separator + scriptFile.getPath());
		}
		res.put("scriptFile", scriptFile);
		res.put("buildListener", listener);
		res.put("schmantHome", schmantHome);
		res.put("systemProperties", getProperties(m_systemProperties));
		res.put("variables", getProperties(m_variables));
		res.put("scriptArguments", m_scriptArguments);
		res.put("taskPackagePath", m_taskPackagePath);
		res.put("scriptEngineName", m_scriptEngineName);
		res.put("verbosity", m_verbosity);
		res.put("buildVariables", build.getBuildVariables());
		res.put("build", build);
		return res;
	}

	private String createAdditionalClasspath(Build<?, ?> build)
	{
		// Have to make a detour around Object to get the compiler to accept
		// the cast.
		File workspaceRoot = new File(((FreeStyleProject) ((Object) build.getParent())).getSomeWorkspace().getRemote());
		if (m_additionalClasspath != null)
		{
			StringBuilder res = new StringBuilder();
			String[] entries = m_additionalClasspath.split("\\Q" + File.pathSeparator + "\\E");
			for (String entry : entries)
			{
				File f = new File(entry);
				if (f.isAbsolute())
				{
					res.append(entry).append(File.pathSeparatorChar);
				}
				else
				{
					// Relative paths are relative to the workspace directory.
					res.append(new File(workspaceRoot, entry).getAbsolutePath()).append(File.pathSeparatorChar);
				}
			}
			return res.toString();
		}
		else
		{
			return null;
		}
	}

	public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException
	{
		if (!validateConfiguration(listener))
		{
			return false;
		}

		File schmantHome = new File(DESCRIPTOR.getSchmantHome());
		listener.getLogger().println("Schmant home: " + schmantHome);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try
		{
			SchmantHomeClassLoader shcl = null;
			try
			{
				InputStream is = SchmantBuilder.class.getResourceAsStream("hudsonSchmantLauncher.jar");
				try
				{
					if (is == null)
					{
						throw new IOException("Cannot find the hudsonSchmantLauncher.jar file. (It should be in the Schmant plugin Jar.)");
					}
					shcl = new SchmantHomeClassLoader(cl, schmantHome, createAdditionalClasspath(build), is);
				}
				finally
				{
					if (is != null)
					{
						is.close();
					}
				}
				Thread.currentThread().setContextClassLoader(shcl);

				Map<String, Object> settings = createLauncherSettings(schmantHome, build, listener);

				Class<?> launcherClass = shcl.loadClass("org.schmant.hudson.launcher.SchmantLauncher");
				Method launcherMethod = findLauncherMethod(launcherClass);
				Object o = launcherClass.newInstance();
				try
				{
					launcherMethod.invoke(o, settings);
				}
				catch (InvocationTargetException e)
				{
					Throwable cause = e.getCause();
					if ((cause != null) && (cause instanceof Exception))
					{
						throw (Exception) cause;
					}
					else
					{
						throw e;
					}
				}
				return true;
			}
			finally
			{
				if (shcl != null)
				{
					shcl.close();
				}
			}
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (ScriptException e)
		{
			e.printStackTrace(listener.fatalError(e.getMessage()));
			return false;
		}
		catch (RuntimeException e)
		{
			e.printStackTrace(listener.fatalError(e.getMessage()));
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace(listener.fatalError(e.getMessage()));
			return false;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(cl);
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
