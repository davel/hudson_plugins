package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;

/**
 * {@inheritDoc}
 */
@Path("/version")
public class DefaultVersionResource {
	private final VersionAssociationCreator versionAssociationCreator;

	public DefaultVersionResource(VersionAssociationCreator versionAssociationCreator) {
		this.versionAssociationCreator = versionAssociationCreator;
	}

	@POST
	@Path("/associate")
	@AnonymousAllowed
	@Consumes(MediaType.APPLICATION_XML)
	public Response associateWithIssues(Project project) {
		System.out.println("###############################");
		versionAssociationCreator.associateFor(project);

		return Response.ok().build();
	}
}