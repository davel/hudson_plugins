package hudson.plugins.ircbot;

import hudson.Plugin;
import hudson.plugins.im.IMPlugin;
import hudson.plugins.ircbot.v2.IRCConnectionProvider;

/**
 * Entry point of the plugin.
 * 
 * @author Renaud Bruyeron
 * @version $Id$
 */
public class PluginImpl extends Plugin {
    
	private transient final IMPlugin imPlugin;

	public PluginImpl() {
		this.imPlugin = new IMPlugin(IRCConnectionProvider.getInstance());
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception {
        super.start();
        this.imPlugin.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
    	this.imPlugin.stop();
        super.stop();
    }

}
