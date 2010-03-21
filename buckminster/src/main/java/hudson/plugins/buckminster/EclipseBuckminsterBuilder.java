package hudson.plugins.buckminster;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.plugins.buckminster.command.CommandLineBuilder;
import hudson.plugins.buckminster.targetPlatform.NoTargetPlatformReference;
import hudson.plugins.buckminster.targetPlatform.TargetPlatformPublisher;
import hudson.plugins.buckminster.targetPlatform.TargetPlatformReference;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tools.ToolProperty;
import hudson.util.DescribableList;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be invoked.
 * 
 * @author Johannes Utzig
 */
public class EclipseBuckminsterBuilder extends Builder {

	private final String installationName, commands, logLevel, params, targetPlatformName;

	@DataBoundConstructor
	public EclipseBuckminsterBuilder(String installationName, String commands, String logLevel, String params, String targetPlatformName) {
		this.installationName = installationName;
		this.commands = commands;
		this.logLevel = logLevel;
		this.params = params;
		this.targetPlatformName = targetPlatformName;
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
	
	public BuckminsterInstallation getInstallation() {
		for (BuckminsterInstallation si : DESCRIPTOR.getBuckminsterInstallations()) {
			if (installationName != null
					&& si.getName().equals(installationName)) {
				return si;
			}
		}

		return null;
	}
	
	public TargetPlatformReference getTargetPlatform() {
		for (TargetPlatformReference reference : DESCRIPTOR.getTargetPlatforms()) {
			if (targetPlatformName != null
					&& reference.getName().equals(targetPlatformName)) {
				return reference;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked") @Override
	public boolean perform(AbstractBuild<?,?> build, Launcher launcher,
			BuildListener listener) {
		//TODO: make this behave in master/slave scenario
		try {
			BuckminsterInstallation installation = getInstallation();
			if(installation==null)
			{

				installation = pickDefault(listener);
				if(installation==null)
				{
					listener.error("No Buckminster Tool Installation has been configured.");
					return false;
				}
			}
	        installation = installation.forNode(Computer.currentComputer().getNode(), listener);
	        installation = installation.forEnvironment(build.getEnvironment(listener));
			String modifiedCommands = getCommands();
			TargetPlatformReference targetPlatform = getTargetPlatform();
			if(targetPlatform!=null && targetPlatform.getPath()!=null){
				modifiedCommands = "setpref targetPlatformPath=\""+targetPlatform.getPath()+"\"" +"\n" + modifiedCommands;
			}
			CommandLineBuilder cmdBuilder = new CommandLineBuilder(installation,modifiedCommands,getLogLevel(),getParams());
			List<String> buildCommands = cmdBuilder.buildCommands(build,listener);
			listener.getLogger().println("Commandline: ");
			for (Iterator iterator = buildCommands.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				listener.getLogger().print(string);
				listener.getLogger().print(" ");
				
			}
			Proc proc = launcher.launch().cmds(buildCommands).stdout(listener).start();
			return proc.join()==0;

		} 
		catch (Exception e) {
			listener.error(e.getLocalizedMessage());
			return false;
		}

	}

	private BuckminsterInstallation pickDefault(BuildListener listener) {
		String oldName = installationName;
		BuckminsterInstallation[] available = DESCRIPTOR.getBuckminsterInstallations();
		if(available==null || available.length==0)
			return null;
		String message = "The selected Buckminster Tool Installation \"{0}\" has not been found, using \"{1}\" instead!";
		message = MessageFormat.format(message, installationName,available[0].getName());
		listener.error(message);
		return available[0];
	}

	/**
	 * Descriptor should be singleton.
	 */
        @Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	/**
	 * Descriptor for {@link HelloWorldBuilder}. Used as a singleton. The class
	 * is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
    @SuppressWarnings("deprecation")
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@CopyOnWrite
		private volatile BuckminsterInstallation[] buckminsterInstallations = new BuckminsterInstallation[0];
		
		@CopyOnWrite
		private volatile EclipseInstallation[] installations = new EclipseInstallation[0];

		DescriptorImpl() {
			super(EclipseBuckminsterBuilder.class);
			load();
		}
		
		/**
		 * Asks hudson for all available {@link AbstractProject}s and iterates over their {@link PublisherList}.
		 * <p>
		 * If a {@link TargetPlatformPublisher} is found and it offers a {@link TargetPlatformReference}, that reference is added to the result list.
		 * 
		 * @return a list of all {@link TargetPlatformReference}s published by a {@link TargetPlatformPublisher}
		 */
		public List<TargetPlatformReference> getTargetPlatforms() {	
			
			List<AbstractProject> projects = Hudson.getInstance().getAllItems(AbstractProject.class);
			List<TargetPlatformReference> references = new ArrayList<TargetPlatformReference>();
			references.add(new NoTargetPlatformReference());
			for (AbstractProject project : projects) {
				DescribableList<Publisher,Descriptor<Publisher>> publishersList = project.getPublishersList();
				for (Describable describable : publishersList) {
					if (describable instanceof TargetPlatformPublisher) {
						TargetPlatformPublisher publisher = (TargetPlatformPublisher) describable;
						TargetPlatformReference reference = publisher.getTargetPlatformReference(project);
						if(reference!=null)
						{
							references.add(reference);	
						}
					}
				}

			}
			return references;
			
		}

		
		public EclipseInstallation[] getInstallations() {
			return installations;
		}
		
		public BuckminsterInstallation[] getBuckminsterInstallations() {
			if(installations!=null && installations.length>0)
			{
				//convert the old installations and reset the installations field
				List<BuckminsterInstallation> converted = convertLegacyInstallations(installations);
				
				if(buckminsterInstallations!=null && buckminsterInstallations.length>0)
				{
					Collections.addAll(converted, buckminsterInstallations);
				}
				buckminsterInstallations = converted.toArray(new BuckminsterInstallation[converted.size()]);
				installations = new EclipseInstallation[0];
			}
			return buckminsterInstallations;
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

			return FormValidation.ok();
		}


		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Run Buckminster";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject o)
				throws FormException {
			save();
			return super.configure(req, o);
		}


		private List<BuckminsterInstallation> convertLegacyInstallations(
				EclipseInstallation[] installations) {
			List<BuckminsterInstallation> convertedInstallations = new ArrayList<BuckminsterInstallation>(installations.length);
			for (EclipseInstallation eclipseInstallation : installations) {
				BuckminsterInstallation inst = new BuckminsterInstallation(eclipseInstallation.getName(), eclipseInstallation.getHome(), eclipseInstallation.getVersion(), eclipseInstallation.getParams(), Collections.<ToolProperty<?>>emptyList());
				convertedInstallations.add(inst);
			}
			return convertedInstallations;
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

		public void setBuckminsterInstallations(
				BuckminsterInstallation... installations) {
            this.buckminsterInstallations = installations;
            save();
			
		}
	}
    
}
