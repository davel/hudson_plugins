package hudson.plugins.buckminster;

import hudson.CopyOnWrite;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Sample {@link Builder}.
 * 
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link EclipseBuckminsterBuilder} is created. The created instance is
 * persisted to the project configuration XML by using XStream, so this allows
 * you to use instance fields (like {@link #name}) to remember the
 * configuration.
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(Build, Launcher, BuildListener)} method will be invoked.
 * 
 * @author Johannes Utzig
 */
public class EclipseBuckminsterBuilder extends Builder {

	private final String installationName, commands, logLevel;

	@DataBoundConstructor
	public EclipseBuckminsterBuilder(String installationName, String commands, String logLevel) {
		this.installationName = installationName;
		this.commands = commands;
		this.logLevel = logLevel;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getEclipseHome() {
		return getInstallation().getHome();
	}

	public String getCommands() {
		return commands;
	}

	public String getLogLevel(){
		if(logLevel!=null && logLevel.length()!=0)
			return logLevel;
		return "info";
	}
	
	public boolean isSelected(String item){
		return getLogLevel().equals(item);
	}
	
	public EclipseInstallation getInstallation() {
		for (EclipseInstallation si : DESCRIPTOR.getInstallations()) {
			if (installationName != null
					&& si.getName().equals(installationName)) {
				return si;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean perform(Build build, Launcher launcher,
			BuildListener listener) {
		//TODO: make this behave in master/slave scenario
		listener.getLogger().println(getEclipseHome());
		listener.getLogger().println("Commands: " + getCommands());
		try {
			List<String> buildCommands = buildCommands(build,listener);
//			launcher.launch(buildCommands.toArray(new String[buildCommands.size()]), null, null, out, workDir)
			ProcessBuilder builder = new ProcessBuilder(buildCommands);
			
			builder.directory(new File(getEclipseHome()));
			builder.redirectErrorStream(true);

			Process process = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String result;
			while ((result = reader.readLine()) != null) {
				listener.getLogger().println(result);
			}
			
			return process.waitFor() == 0;
		} catch (Exception e) {
			listener.error(e.getLocalizedMessage());
			return false;
		}

	}

	/**
	 * fills an arraylist with all program arguments for buckminster
	 * 
	 * @param build
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	private List<String> buildCommands(Build build, BuildListener listener)
			throws MalformedURLException, IOException, InterruptedException{

		// the file listing all the commands since buckminster doesn't accept
		// several commands as programm arguments
		String commandsPath = build.getRootDir().getAbsolutePath()
				+ "/commands.txt";
		List<String> commandList = new ArrayList<String>();

		// VM Options
		commandList.add("java");
		String params = getInstallation().getParams();
		String[] additionalParams = params.split("[\n\r]+");
		for (int i = 0; i < additionalParams.length; i++) {
			if(additionalParams[i].trim().length()>0)
				commandList.add(additionalParams[i]);
		}
		if(additionalParams.length==0)
		{
			commandList.add("-Xmx512m");
			commandList.add("-XX:PermSize=128m");
		}
		commandList.add("-jar");
		commandList.add(findEquinoxLauncher());

		// Specify Eclipse Product
		commandList.add("-application");
		commandList.add("org.eclipse.buckminster.cmdline.headless");

		// set the workspace to the hudson workspace
		commandList.add("-data");
		String workspace = null;
	
		workspace = build.getProject().getWorkspace().toURI().getPath();

		commandList.add(workspace);


		 commandList.add("--loglevel");
		 commandList.add(getLogLevel());

		// Tell Buckminster about the command file
		commandList.add("-S");
		commandList.add(commandsPath);

		//TODO: make this behave in master/slave scenario
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(commandsPath));

			String[] commands = getCommands().split("[\n\r]+");
			for (int i = 0; i < commands.length; i++) {
				if (!commands[i].startsWith("perform"))
					// the command is not perform -> nothing to modify
					writer.println(commands[i]);
				else {
					// perform will usually produce build artifacts
					// set the buckminster.output.root to the job's workspace
					writer.print("perform -D buckminster.output.root=\""
							+ build.getProject().getWorkspace().toURI().getPath()+"/buckminster.output\"");
					writer.println(commands[i].replaceFirst("perform", ""));
					// TODO: let the user set more properties

				}
			}
		} finally {
			if (writer != null)
				writer.close();
		}

		return commandList;
	}

	/**
	 * searches for the eclipse starter jar
	 * <p>
	 * The content of the folder $ECLIPSE_HOME/plugins is listed and the first
	 * file that starts with <code>org.eclipse.equinox.launcher_</code> is
	 * returned.
	 * 
	 * @return the guess for the startup jar, or <code>null</code> if none was
	 *         found
	 * @see EclipseBuckminsterBuilder#getEclipseHome()
	 */
	private String findEquinoxLauncher() {
		//TODO: make this behave in master/slave scenario
		File pluginDir = new File(getEclipseHome() + "/plugins");
		File[] plugins = pluginDir.listFiles();
		for (int i = 0; i < plugins.length; i++) {
			if (plugins[i].getName()
					.startsWith("org.eclipse.equinox.launcher_")) {
				return "plugins/" + plugins[i].getName();

			}
		}
		return null;
	}


	public Descriptor<Builder> getDescriptor() {
		// see Descriptor javadoc for more about what a descriptor is.
		return DESCRIPTOR;
	}

	/**
	 * Descriptor should be singleton.
	 */
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	/**
	 * Descriptor for {@link HelloWorldBuilder}. Used as a singleton. The class
	 * is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@CopyOnWrite
		private volatile EclipseInstallation[] installations = new EclipseInstallation[0];

		DescriptorImpl() {
			super(EclipseBuckminsterBuilder.class);
			load();
		}

		public EclipseInstallation[] getInstallations() {
			return installations;
		}

		/**
		 * Performs on-the-fly validation of the form field 'installationName'.
		 * 
		 * @param value
		 *            This receives the current value of the field.
		 */
		public FormValidation doCheckEclipseHome(StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException {
			final String value = req.getParameter("value");
			File eclipseHome = new File(value);
			if (!eclipseHome.exists()) {
				String error = "The path {0} does not exist";
				error = MessageFormat.format(error, value);
				return FormValidation.error(error);
			} else if (!eclipseHome.isDirectory()) {
				String error = "{0} is not a directory";
				error = MessageFormat.format(error, value);
				return FormValidation.error(error);
			} else if (!new File(eclipseHome.getAbsolutePath()
					+ "/plugins").exists()) {
				String error = "{0} does not contain a plugins directory";
				error = MessageFormat.format(error, value);
				return FormValidation.error(error);
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckEclipseVersion(StaplerRequest req,
				StaplerResponse rsp) throws IOException, ServletException {
			final String version = req.getParameter("value");
			if (!(version.startsWith("3.4") || version.startsWith("3.5"))) {
				return FormValidation.error("Eclipse version is not valid. Currently only 3.4.x and 3.5 are supported.");
				
			}
;
			return FormValidation.ok();
		}


		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Run Buckminster";
		}

		public boolean configure(StaplerRequest req, JSONObject o)
				throws FormException {
			// to persist global configuration information,
			// set that to properties and call save().
			installations = req.bindParametersToList(EclipseInstallation.class,
					"eclipse.").toArray(new EclipseInstallation[0]);
			save();
			return super.configure(req, o);
		}


		
		@Override
		public Builder newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			
			return req.bindJSON(EclipseBuckminsterBuilder.class,
			formData);
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return FreeStyleProject.class.isAssignableFrom(jobType);
		}
	}
}
