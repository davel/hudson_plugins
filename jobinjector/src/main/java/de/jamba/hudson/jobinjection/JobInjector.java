package de.jamba.hudson.jobinjection;

import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.tasks.Builder;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;

import org.kohsuke.stapler.StaplerRequest;

/**
 * This builder will start a new job and wait for its completion. The job will
 * be started on those node (group) where it was configured to run,
 */
public class JobInjector extends Builder implements Serializable, JobInjectorListenerInterface {
	// Which job to start and wait for?
	private String startJobName;
	// Has the started job finished yet?
	private boolean done = false;

	JobInjector(String startJobName) {
		this.startJobName = startJobName;
	}

	/**
	 * Returns the name of the job to start and wait for
	 *
	 * @return the name of the job to start and wait for
	 */
	public String getStartJobName() {
		return startJobName;
	}

	/*
	 * Suspends current thread for a given number of seconds.
	 * Should not be interrupted.
	 */
	private void sleep(int seconds) {
		if (seconds > 0)
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException ignore) {
			}
	}

	/**
	 * Callback method to allow a listener to notify us about job completion.
	 */
	public void wakeMeUp() {
		done = true;
	}

	/**
	 * This method does the real job.
	 */
	public boolean perform(Build build, Launcher launcher, BuildListener listener) {
		PrintStream logger = listener.getLogger();
		// Find the right project (= job to run).
		Project proj = null;
		for (Project p : Hudson.getInstance().getProjects())
			if (p.getFullName().equals(getStartJobName())) {
				proj = p;
				break;
			}
		if (proj == null) {
			logger.println("Project \"" + getStartJobName() + "\" does not exist.");
			// Do not continue with the build process.
			return false;
		}
		// Create a new listener
		JobInjectorListener rrl = new JobInjectorListener(Build.class, getStartJobName(), this);
		rrl.register();
		// Start new job (= run of a project)
		String builtOn = build.getBuiltOnStr();
		logger.println("Starting run of project " + getStartJobName() + " at "
				+ new Date() + " on " + (builtOn == null ? "Master" : builtOn)
				+ '.');
		done = false;
		proj.scheduleBuild(); // Will start a job on a node (as configured).
		// Wait until started job is done
		do {
			sleep(10); // This is not Thread.sleep(milliseconds).
		} while (!done);
		logger.println("Project " + getStartJobName() + " finished at "
				+ new Date() + '.');
		// Clean up
		rrl.unregister();
		rrl = null;
		// Continue with the build process.
		return true;
	}

	/**
	 * Returns a JobInjector descriptor.
	 */
	public Descriptor<Builder> getDescriptor() {
		return DESCRIPTOR;
	}

	/**
	 * Descriptor should be singleton.
	 */
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	/**
	 * Descriptor for {@link JobInjector}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	public static final class DescriptorImpl extends Descriptor<Builder> {
		DescriptorImpl() {
			super(JobInjector.class);
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Start a job and wait for its completion";
		}

		/**
		 * Creates a new instance of {@link JobInjector} from a submitted form.
		 */
		public JobInjector newInstance(StaplerRequest req) throws FormException {
			return new JobInjector(req.getParameter("startJobName"));
		}
	}
}
