package com.redfin.hudson;

import hudson.model.Cause;

import java.net.URL;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class UrlChangeCause extends Cause {
    private final URL url;

    public UrlChangeCause(URL url) {
        this.url = url;
    }

    @Override
    public String getShortDescription() {
        return Messages.UrlChangeCause_Description(url!=null?url:"remote URL");
    }
}
