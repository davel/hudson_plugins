package de.jamba.hudson.jobinjection;

import hudson.model.Build;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * This class listens for completion of a JobInjector build run.
 */
public class JobInjectorListener extends RunListener<Build> {

	private String waitForJobName;
	private JobInjectorListenerInterface jif;

	/**
	 * Simple constructor.
	 */
	public JobInjectorListener(Class<Build> targetType, String waitForJobName,
			JobInjectorListenerInterface jif) {
		super(targetType);
		this.waitForJobName = waitForJobName;
		this.jif = jif;

	}

	/**
	 * Method will be called on build completion.
	 */
	public void onCompleted(Build r, TaskListener listener) {
		if (r.toString().startsWith(waitForJobName + ' ')) {
			jif.wakeMeUp();
		}
	}
}
