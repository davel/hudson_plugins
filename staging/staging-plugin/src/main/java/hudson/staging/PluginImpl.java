package hudson.staging;

import hudson.Plugin;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStep.PublisherList;

public class PluginImpl extends Plugin {

	@Override
	public void start() throws Exception {
		
		Publisher.PUBLISHERS.addRecorder(StagingPublisher.DESCRIPTOR);
		
	}

}
