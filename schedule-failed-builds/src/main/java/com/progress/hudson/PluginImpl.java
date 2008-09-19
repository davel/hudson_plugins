package com.progress.hudson;


import hudson.Plugin;
import hudson.tasks.BuildStep;
import hudson.triggers.Triggers;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Stefan Fritz <sfritz@progress.com>
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        Triggers.TRIGGERS.add(ScheduleFailedBuildsTrigger.DESCRIPTOR);        
        BuildStep.PUBLISHERS.add(ScheduleFailedBuildsPublisher.DESCRIPTOR);
    }
}
