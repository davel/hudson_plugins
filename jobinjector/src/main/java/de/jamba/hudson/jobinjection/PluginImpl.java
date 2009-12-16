package de.jamba.hudson.jobinjection;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point of the job injection plugin.
 * <p>
 * The plugin will start a new job and wait for its completion.
 * The job will be started on those node (group) where it was configured to run.
 * 
 * @plugin
 */
public class PluginImpl extends Plugin {
	
	/**
	 *  This plugin is a Hudson "Builder",
	 *  so add it to the BUILDERS list at startup.
	 */
    public void start() throws Exception {
        BuildStep.BUILDERS.add(JobInjector.DESCRIPTOR);
    }
}
