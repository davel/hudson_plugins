package de.fspengler.hudson.plugin;

import hudson.Plugin;
import hudson.model.Jobs;
import hudson.model.listeners.RunListener;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
    	
    	Jobs.PROPERTIES.add(LogActionProperty.DESCRIPTOR);
    	RunListener.LISTENERS.add(new LogItemListener());
    	
    }
}
