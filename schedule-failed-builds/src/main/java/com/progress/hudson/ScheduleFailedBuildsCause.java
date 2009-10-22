package com.progress.hudson;

import hudson.model.Cause;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class ScheduleFailedBuildsCause extends Cause {

    @Override
    public String getShortDescription() {
        return Messages.ScheduleFailedBuildsCause_Description();
    }
}
