package de.fspengler.hudson.plugin;

import hudson.model.Cause;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class LogActionCause extends Cause{

    @Override
    public String getShortDescription() {
        return Messages.LogActionCause_ShortDescription();
    }
}
