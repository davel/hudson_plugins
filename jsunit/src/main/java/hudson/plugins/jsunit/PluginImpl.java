package hudson.plugins.jsunit;

import hudson.Plugin;

import hudson.tasks.BuildStep;

/**
 * 
 * Entry point of a plugin.
 * 
 * 
 * 
 * <p>
 * 
 * There must be one {@link Plugin} class in each plugin.
 * 
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 * 
 * 
 * 
 * @author Erik Ramfelt
 * 
 * @plugin jsUnit
 * 
 */

public class PluginImpl extends Plugin {

    @Override
    public void start() throws Exception {

        BuildStep.PUBLISHERS.addRecorder(JSUnitPublisher.DESCRIPTOR);

    }

}
