package hudson.plugins.buckminster;

import hudson.CopyOnWrite;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.plugins.buckminster.command.CommandLineBuilder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
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

	private final String installationName, commands, logLevel, params;

	@DataBoundConstructor
	public EclipseBuckminsterBuilder(String installationName, String commands, String logLevel, String params) {
		this.installationName = installationName;
		this.commands = commands;
		this.logLevel = logLevel;
		this.params = params;
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
	
	public String getParams() {
		return params;
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
		try {
			CommandLineBuilder cmdBuilder = new CommandLineBuilder(getInstallation(),getCommands(),getLogLevel(),getParams());
			List<String> buildCommands = cmdBuilder.buildCommands(build,listener);
			listener.getLogger().println("Commandline: ");
			for (Iterator iterator = buildCommands.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				listener.getLogger().print(string);
				listener.getLogger().print(" ");
				
			}
//			launcher.launch(buildCommands.toArray(new String[buildCommands.size()]), null, null, out, workDir)
			ProcessBuilder builder = new ProcessBuilder(buildCommands);
			
			builder.directory(new File(getEclipseHome()));
			builder.redirectErrorStream(true);

			Process process = builder.start();
			ProcessStreamLogger streamLogger = new ProcessStreamLogger(process, listener);
			streamLogger.start();
			try{
				return process.waitFor() == 0;
			}catch(InterruptedException e){
				listener.getLogger().println("Build Interrupted");
				process.destroy();
				return false;
			}
		} 
		catch (Exception e) {
			listener.error(e.getLocalizedMessage());
			return false;
		}

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
			return FreeStyleProject.class.isAssignableFrom(jobType) || MatrixProject.class.isAssignableFrom(jobType);
		}
	}
}
