package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson;

import java.io.IOException;
import java.util.logging.Logger;

import hudson.Plugin;

/**
 * Main {@link Plugin} implementation for the Jira Project Key plugin
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 * @plugin
 */
public class PluginImpl extends Plugin {

	private static final Logger LOGGER = Logger.getLogger(PluginImpl.class.getName());

	private static final String DEFAULT_CACHE_FILENAME = "jira-issue-index.xml";

	private String indexFilename;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
		load();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void load() throws IOException {
		super.load();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws Exception {
		save();
		super.stop();
	}
}
