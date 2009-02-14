package org.wirsind.info;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Rhapsody Plugin Entry Point
 * @author Markus Hoffmann
 */
public class PluginImpl extends Plugin {
	
    public void start() throws Exception {
        BuildStep.BUILDERS.add(RhapsodyBuilder.DESCRIPTOR);
    }
}
