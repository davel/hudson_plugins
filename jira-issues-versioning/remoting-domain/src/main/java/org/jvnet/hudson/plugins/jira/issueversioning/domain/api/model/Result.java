package org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model;

/**
 * Enum for Result statuses
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
public enum Result {

	SUCCESS("blue.gif"),
	FAILURE("red.gif"),
	UNSTABLE("yellow.gif"),
	NOT_BUILT("grey.gif"),
	ABORTED("red.gif");

	private String icon;

	/**
	 * Constructor
	 * 
	 * @param icon icon name
	 */
	private Result(String icon) {
		this.icon = icon;
	}

	/**
	 * Gets the icon name
	 * 
	 * @return the icon name
	 */
	public String getIcon() {
		return icon;
	}
	
}
