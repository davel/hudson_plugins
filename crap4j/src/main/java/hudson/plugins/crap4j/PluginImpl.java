package hudson.plugins.crap4j;

import hudson.Extension;
import hudson.Plugin;

/**
 * Entry point for plugin.
 * Add a publisher for crap.
 */
@Extension
public class PluginImpl extends Plugin {
	
	public PluginImpl() {
		super();
	}
	
//	@Override
//	public void start() throws Exception {
//		Publisher.PUBLISHERS.addRecorder(Crap4JPublisher.DESCRIPTOR);
//	}
}
