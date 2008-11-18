package com.mtvi.plateng.hudson.springbeandoc;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point of a plugin.
 * 
 * @author edelsonj
 * @plugin
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addNotifier(SpringBeanDocArchiver.DESCRIPTOR);
    }
}
