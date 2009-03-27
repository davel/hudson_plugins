package hudson.plugins.easyant;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormFieldValidator;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A builder for EasyAnt scripts
 * 
 * @author Jean Louis Boudart
 */
public class EasyAnt extends Builder {

	private final String name;

	private final String targets;

	private final String buildFile;

	@DataBoundConstructor
	public EasyAnt(String name, String targets, String buildFile) {
		this.name = name;
		this.targets = targets;
		this.buildFile = buildFile;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getName() {
		return name;
	}

	public String getTargets() {
		return targets;
	}

	public String getBuildFile() {
		return buildFile;
	}

	public EasyAntInstallation getEasyAnt() {
		for (EasyAntInstallation i : DESCRIPTOR.getInstallations()) {
			if (name != null && i.getName().equals(name))
				return i;
		}
		return null;

	}

	public boolean perform(Build<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		Project proj = build.getProject();

		ArgumentListBuilder args = new ArgumentListBuilder();

		String execName;
		if (launcher.isUnix())
			execName = "easyant";
		else
			execName = "easyant.bat";

		String normalizedTargets = targets.replaceAll("[\t\r\n]+", " ");

		EasyAntInstallation ai = getEasyAnt();
		if (ai == null) {
			args.add(execName);
		} else {
			File exec = ai.getExecutable();
			if (!ai.isAvailable()) {
				listener.fatalError(exec + " doesn't exist");
				return false;
			}
			args.add(exec.getPath());
		}
		args.addKeyValuePairs("-D", build.getBuildVariables());
		args.addTokenized(normalizedTargets);
		EnvVars env = build.getEnvironment();
		if (ai != null)
			env.put("EASYANT_HOME", ai.getEasyantHome());

		if (!launcher.isUnix()) {
			// on Windows, executing batch file can't return the correct error
			// code,
			// so we need to wrap it into cmd.exe.
			// double %% is needed because we want ERRORLEVEL to be expanded
			// after
			// batch file executed, not before. This alone shows how broken
			// Windows is...
			args.prepend("cmd.exe", "/C");
			args.add("&&", "exit", "%%ERRORLEVEL%%");
		}

		FilePath rootLauncher = null;
		if (buildFile != null && buildFile.trim().length() != 0) {
			String rootBuildScriptReal = Util.replaceMacro(buildFile, env);
			rootLauncher = new FilePath(proj.getModuleRoot(), new File(
					rootBuildScriptReal).getParent());
		} else {
			rootLauncher = proj.getModuleRoot();
		}

		try {
			int r = launcher.launch(args.toCommandArray(), env,
					listener.getLogger(), rootLauncher).join();
			return r == 0;
		} catch (IOException e) {
			Util.displayIOException(e, listener);
			e.printStackTrace(listener.fatalError("command execution failed"));
			return false;
		}
	}

	public Descriptor<Builder> getDescriptor() {
		return DESCRIPTOR;
	}

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		@CopyOnWrite
		private volatile EasyAntInstallation[] installations = new EasyAntInstallation[0];

		public DescriptorImpl() {
			load();
		}

		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		protected DescriptorImpl(Class<? extends EasyAnt> clazz) {
			super(clazz);
		}

		public String getHelpFile() {
			return "/plugin/easyant/help.html";
		}

		public String getDisplayName() {
			return Messages.EasyAnt_DisplayName();
		}

		public EasyAntInstallation[] getInstallations() {
			return installations;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			installations = req.bindJSONToList(EasyAntInstallation.class,
					json.get("inst")).toArray(new EasyAntInstallation[0]);
			save();
			return true;
		}

		public EasyAnt newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return (EasyAnt) req.bindJSON(clazz, formData);
		}

		/**
		 * Checks if the specified Hudson EASYANT_HOME is valid.
		 */
		public void doCheckEasyAntHome(StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException {

			new FormFieldValidator(req, rsp, true) {
				public void check() throws IOException, ServletException {
					File f = getFileParameter("value");

					if (!f.isDirectory()) {
						error(f + " is not a directory");
						return;
					}

					if (!new File(f, "bin").exists()
							&& !new File(f, "lib").exists()) {
						error(f + " doesn't look like an EasyAnt directory");
						return;
					}

					if (!new File(f, "bin/easyant").exists()) {
						error(f + " doesn't look like an EasyAnt directory");
						return;
					}

					ok();
				}
			}.process();
		}

	}

}
