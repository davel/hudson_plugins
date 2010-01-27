package org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest;

import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author Stig Kleppe-Jorgensen, 2009.12.28
 * @fixme add description
 */
public class BuildTest {
	@Test
	public void should_use_content_for_equals() {
		checkEquals(true, 1, 1, "DEV-345", "DEV-345");
		checkEquals(false, 1, 2, "DEV-345", "DEV-345");
		checkEquals(false, 2, 1, "DEV-345", "DEV-345");
		checkEquals(false, 1, 1, "DEV-346", "DEV-345");
		checkEquals(false, 1, 1, "DEV-345", "DEV-346");
		checkEquals(false, 2, 1, "DEV-345", "DEV-346");
		checkEquals(false, 1, 2, "DEV-346", "DEV-345");
	}

	private void checkEquals(boolean equal,
	                         final int number1, final int number2,
	                         final String issue1, final String issue2) {
		final Build build1 = new Build(number1, createIssues(issue1));
		final Build build2 = new Build(number2, createIssues(issue2));

		if (equal) {
			assertThat(build1, equalTo(build2));
		} else {
			assertThat(build1, not(equalTo(build2)));
		}
	}

	private Set<String> createIssues(final String issue) {
		return Sets.newHashSet("DEV-123", "DEV-234", issue);
	}
}