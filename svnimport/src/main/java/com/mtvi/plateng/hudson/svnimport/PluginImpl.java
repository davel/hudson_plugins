package com.mtvi.plateng.hudson.svnimport;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * 
 * @author edelsonj
 * @plugin
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addNotifier(SubversionImporter.DescriptorImpl.INSTANCE);
    }
}
