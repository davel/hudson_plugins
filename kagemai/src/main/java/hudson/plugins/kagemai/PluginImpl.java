package hudson.plugins.kagemai;

import hudson.Plugin;
import hudson.model.Jobs;
import hudson.tasks.BuildStep;

/**
 * Entry point of a plugin.
 * 
 * @author yamkazu
 * 
 */
public class PluginImpl extends Plugin {

	@Override
	public void start() throws Exception {
		Jobs.PROPERTIES.add(KagemaiProjectProperty.DESCRIPTOR);
		new KagemaiChangelogAnnotator().register();
		BuildStep.PUBLISHERS.add(KagemaiPublisher.DESCRIPTOR);
	}

}