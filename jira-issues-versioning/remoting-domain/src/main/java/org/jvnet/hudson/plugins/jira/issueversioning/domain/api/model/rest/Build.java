package org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

/**
 * @author Stig Kleppe-Jorgensen, 2009.12.28
 * @fixme add description
 */
@XmlRootElement
public final class Build {
	public static final Build EMPTY = new Build();

	@XmlElement
	private final int number;
	@XmlElement
	private final Set<String> issues;

	/**
	 * JAXB need default constructor to work.
	 */
	private Build() {
		number = -1;
		issues = new HashSet<String>();
	}

	public Build(int number, Set<String> issues) {
		Preconditions.checkArgument(number > 0, "number must be greater than 0");
		Preconditions.checkNotNull(issues, "issues cannot be null");

		this.number = number;
		this.issues = issues;
	}

	public int getNumber() {
		return number;
	}

	public Set<String> getIssues() {
		return Collections.unmodifiableSet(issues);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Build build = (Build) o;

		return number == build.number && issues.equals(build.issues);

	}

	@Override
	public int hashCode() {
		return 31 * number + issues.hashCode();
	}
}
