package hudson.plugins.kagemai;

import hudson.Plugin;
import hudson.model.Jobs;
import hudson.tasks.BuildStep;

/**
 * Entry point of a plugin.
 * 
 * @author yamkazu
 * @Plugin
 */
public class PluginImpl extends Plugin {

	private KagemaiChangelogAnnotator annotator = new KagemaiChangelogAnnotator();

	@Override
	public void start() throws Exception {
		annotator.register();
		BuildStep.PUBLISHERS.add(KagemaiPublisher.DESCRIPTOR);
		Jobs.PROPERTIES.add(KagemaiProjectProperty.DESCRIPTOR);
	}

	@Override
	public void stop() throws Exception {
		annotator.unregister();
	}

}