package hudson.plugins.rotatews;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.tasks.BuildStep;

/**
 * @Plugin
 */
public class PluginImpl extends Plugin {
	
	@Override
	public void start() throws Exception {
		BuildStep.PUBLISHERS.addNotifier(Rotate.DESCRIPTOR);
	}
}
