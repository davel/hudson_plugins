package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest.exceptions;

/**
 * REST API Not Authorised exception
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
public class NotAuthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message the Exception message
	 */
	public NotAuthorizedException(String message) {
		super(message);
	}

}
