package com.redfin.hudson;

import hudson.model.Cause;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class UrlChangeCause extends Cause {

    @Override
    public String getShortDescription() {
        return Messages.UrlChangeCause_Description();
    }
}
