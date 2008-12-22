package de.stephannoske.hudson.tools;

import hudson.Plugin;
import hudson.tasks.BuildStep;
 
/**
 * Entry point of a plugin.
 *
 * @author Stephan Noske
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addNotifier(NabatzagPublisher.DESCRIPTOR); 
    }
}
