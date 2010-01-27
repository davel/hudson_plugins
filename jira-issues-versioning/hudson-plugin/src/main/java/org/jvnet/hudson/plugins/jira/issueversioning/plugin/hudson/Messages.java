package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson;

import org.jvnet.localizer.ResourceBundleHolder;

/**
 * Internationalisation implementation for messages within the Jira Project Key plugin
 *  
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
public class Messages {

	private static final ResourceBundleHolder HOLDER = ResourceBundleHolder.get(Messages.class);

	/**
	 * Gets the {@link JiraProjectKeyJobProperty} display name
	 * 
	 * @return the display name
	 */
	public static String getJiraKeyPropertyDisplayName() {
		return HOLDER.format("JiraKeyProperty.DisplayName", new Object[0]);
	}

	/**
	 * Gets the {@link JiraIssueIndexerRecorder} display name
	 * 
	 * @return the display name
	 */
	public static String getJiraIssueIndexerDisplayName() {
		return HOLDER.format("JiraIssueIndexer.DisplayName", new Object[0]);
	}

}
