package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest;

import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;

/**
 * @author Stig Kleppe-Jorgensen, 2009.12.29
 * @fixme add description
 */
public interface VersionAssociationCreator {
	/**
	 * Associates the given project's issues with a new or existing version and releases it. The versions name is taken
	 * from the Hudson release number.
	 */
	void associateFor(Project project);
}
