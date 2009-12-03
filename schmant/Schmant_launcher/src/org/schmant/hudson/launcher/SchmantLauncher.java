package org.schmant.hudson.launcher;

import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import javax.script.SimpleBindings;

import org.schmant.Launcher;
import org.schmant.LauncherSettings;

/**
 * This is used by the Schmant builder to launch Schmant.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class SchmantLauncher
{
	private File getFileValue(Map<String, Object> settings, String key)
	{
		Object res = settings.get(key);
		if (res == null)
		{
			throw new IllegalArgumentException("Missing the " + key + " configuration setting");
		}
		return (File) res;
	}
	
	private BuildListener getBuildListener(Map<String, Object> settings)
	{
		return (BuildListener) settings.get("buildListener");
	}
	
	private void setUnlessAlreadySet(Map<String, Object> settings, LauncherSettings ls, String key, String value)
	{
		if (ls.getProperties().containsKey("key"))
		{
			getBuildListener(settings).getLogger().println("[WARN] The " + key + " property is set by the user. Will not set it to " + value);
		}
		else
		{
			ls.addProperty(key, value);
		}
	}
	
	private LauncherSettings createLauncherSettings(Map<String, Object> settings)
	{
		LauncherSettings res = new LauncherSettings();
		res.setSchmantHome(getFileValue(settings, "schmantHome"));
		res.setScriptFile(getFileValue(settings, "scriptFile").getPath());
		Properties variables = (Properties) settings.get("variables");
		if (variables != null)
		{
			for (Map.Entry<Object, Object> me : variables.entrySet())
			{
				res.addProperty(me.getKey().toString(), me.getValue().toString());
			}
		}
		
		String scriptArguments = (String) settings.get("scriptArguments");
		if (scriptArguments != null)
		{
			for (String arg : scriptArguments.split(" +"))
			{
				res.addProgramArgument(arg);
			}
		}
		
		String taskPackagePath = (String) settings.get("taskPackagePath");
		if (taskPackagePath != null)
		{
			res.setTaskPackagePath(taskPackagePath);
		}
		
		String scriptEngineName = (String) settings.get("scriptEngineName");
		if ((scriptEngineName != null) && (scriptEngineName.length() > 0))
		{
			res.setScriptEngineName(scriptEngineName);
		}
		String verbosity = (String) settings.get("verbosity");
		if (verbosity != null)
		{
			if ("Trace".equals(verbosity))
			{
				res.increaseVerbosityLevel();
				res.increaseVerbosityLevel();
			}
			else if ("Debug".equals(verbosity))
			{
				res.increaseVerbosityLevel();
			}
			else if ("Warnings".equals(verbosity))
			{
				res.decreaseVerbosityLevel();
			}
			else if ("Errors".equals(verbosity))
			{
				res.decreaseVerbosityLevel();
				res.decreaseVerbosityLevel();
			}
			else if ("Off".equals(verbosity))
			{
				res.decreaseVerbosityLevel();
				res.decreaseVerbosityLevel();
				res.decreaseVerbosityLevel();
			}
		}
		
		Build<?, ?> b = (Build<?, ?>) settings.get("build");
		if (b != null)
		{
			setUnlessAlreadySet(settings, res, "buildNumber", Integer.toString(b.getNumber()));
			setUnlessAlreadySet(settings, res, "buildId", b.getId());
			setUnlessAlreadySet(settings, res, "jobName", b.getParent().getName());
			setUnlessAlreadySet(settings, res, "buildTag", "hudson-" + b.getParent().getName() + "-" + b.getNumber());
			setUnlessAlreadySet(settings, res, "executorNumber", Integer.toString(b.getExecutor().getNumber()));
			setUnlessAlreadySet(settings, res, "workspace", ((FreeStyleProject) b.getParent()).getWorkspace().getRemote());
		}
		return res;
	}
	
	private Properties setSystemProperties(Properties p)
	{
		Properties res = new Properties();
		Properties sysProps = System.getProperties();
		for (Map.Entry<Object, Object> me : p.entrySet())
		{
			if (sysProps.containsKey(me.getKey()))
			{
				// Save it
				res.put(me.getKey(), sysProps.get(me.getKey()));
			}
			sysProps.setProperty(me.getKey().toString(), me.getValue().toString());
		}
		return res;
	}
	
	private SimpleBindings createSimpleBindings(Map<String, Object> settings)
	{
		SimpleBindings res = new SimpleBindings();
		res.put("buildVariables", settings.get("buildVariables"));
		return res;
	}
	
	public void launch(Map<String, Object> settings) throws Exception
	{
		Properties oldSystemProperties = setSystemProperties((Properties) settings.get("systemProperties"));
		try
		{
			new Launcher().launch(createLauncherSettings(settings), createSimpleBindings(settings), new HudsonReportFactory(getBuildListener(settings)), null);
		}
		finally
		{
			setSystemProperties(oldSystemProperties);
		}
	}
}
