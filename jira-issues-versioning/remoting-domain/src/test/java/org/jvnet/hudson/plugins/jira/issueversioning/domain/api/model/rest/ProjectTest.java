package org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest;

import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test of {@link Project}.
 *
 * @author Stig Kleppe-Jorgensen, 2009.12.28
 */
public class ProjectTest {
	@Test
	public void should_use_content_for_equals() {
		final Project project1 = new Project("project1", "4.22", createBuild("DEV-234"));
		final Project project2 = new Project("project1", "4.22", createBuild("DEV-234"));
		final Project project3 = new Project("project2", "4.22", createBuild("DEV-234"));
		final Project project4 = new Project("project1", "4.22", createBuild("DEV-235"));

		assertThat(project1, equalTo(project2));
		assertThat(project1, not(equalTo(project3)));
		assertThat(project1, not(equalTo(project4)));
	}

	@Test
	public void should_get_list_with_all_issues() {
		final Project project = new Project("project1", "4.22", createBuild("DEV-234"));
		project.addFailedBuild(createBuild("DEV-132"));

		assertThat(project.getAllIssues().size(), is(4));
	}

	private Build createBuild(final String issue) {
		return new Build(1, createIssues(issue));
	}

	private Set<String> createIssues(final String issue) {
		return Sets.newHashSet("DEV-123", issue, "DEV-345");
	}
}