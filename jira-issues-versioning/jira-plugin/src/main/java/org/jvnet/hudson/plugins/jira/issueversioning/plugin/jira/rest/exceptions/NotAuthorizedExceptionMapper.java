package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.atlassian.plugins.rest.common.Status;

/**
 * REST API {@link NotAuthorizedException} {@link ExceptionMapper}
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

	/**
	 * {@inheritDoc}
	 */
	public Response toResponse(NotAuthorizedException exception) {
		return Status.forbidden().message(exception.getMessage()).response();
	}

}
