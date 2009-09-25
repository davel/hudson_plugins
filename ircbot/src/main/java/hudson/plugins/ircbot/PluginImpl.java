package hudson.plugins.ircbot;

import hudson.Plugin;

/**
 * Entry point of the plugin.
 * 
 * @author Renaud Bruyeron
 * @version $Id$
 * @plugin
 */
public class PluginImpl extends Plugin {
    
    /**
     * @see hudson.Plugin#stop()
     */
    @Override
    public void stop() throws Exception {
        IrcPublisher.DESCRIPTOR.stop();
    }

}
