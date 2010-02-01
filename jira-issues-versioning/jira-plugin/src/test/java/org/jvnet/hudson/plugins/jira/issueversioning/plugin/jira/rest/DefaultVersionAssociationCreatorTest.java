package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.cache.CacheManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Build;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link DefaultVersionAssociationCreator}
 *
 * @author Stig Kleppe-Jorgensen, 2009.12.30
 */
public class DefaultVersionAssociationCreatorTest {
	private VersionManager versionManager;
	private IssueManager issueManager;
	private FieldManager fieldManager;
	private CacheManager cacheManager;
	private IssueIndexManager issueIndexManager;

	@Before
	public void setupMocks() {
		versionManager = mock(VersionManager.class);
		issueManager = mock(IssueManager.class);
		fieldManager = mock(FieldManager.class);
		cacheManager = mock(CacheManager.class);
		issueIndexManager = mock(IssueIndexManager.class);
	}

	@Test
	public void should_release_new_version_for_given_issues_with_correct_status() throws CreateException, IndexException {
		setupState();

		createVac().associateFor(createProject("1.2.5"));

		assertState("1.2.5");
	}

	@Test
	public void should_release_existing_version_for_given_issues_with_correct_status() throws CreateException, IndexException {
		setupState();

		createVac().associateFor(createProject("1.3"));

		assertState("1.3");
	}

	private void setupState() throws CreateException {
		when(issueManager.getIssueObject(anyString())).thenAnswer(new Answer<Issue>() {
			public Issue answer(InvocationOnMock invocationOnMock) throws Throwable {
				final String issueKey = (String) invocationOnMock.getArguments()[0];
				int statusId = IssueFieldConstants.INPROGRESS_STATUS_ID;

				if (issueKey.startsWith("DEV-12")) {
					statusId = IssueFieldConstants.RESOLVED_STATUS_ID;
				}

				return mockedIssue(issueKey, statusId);
			}
		});

		when(versionManager.createVersion(eq("1.2.5"), (Date) anyObject(), eq(""), (Long) anyObject(), (Long) anyObject())).thenAnswer(mockedVersionAnswer(3, 0));
		when(versionManager.getVersion((Long)anyObject(), eq("1.3"))).thenAnswer(mockedVersionAnswer(0, 1));

		final FixVersionsSystemField fixVersionsSystemField = mock(FixVersionsSystemField.class);
		when(fieldManager.getField(anyString())).thenReturn(fixVersionsSystemField);
	}

	private Answer<Version> mockedVersionAnswer(final int projectIdIndex, final int versionNameIndex) {
		return new Answer<Version>() {
			public Version answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Long projectId = (Long) invocationOnMock.getArguments()[projectIdIndex];
				final String versionName = (String) invocationOnMock.getArguments()[versionNameIndex];

				return mockedVersion(projectId, versionName);
			}
		};
	}

	private void assertState(final String versionName) throws IndexException {
		final ArgumentCaptor<Version> releaseArgument = ArgumentCaptor.forClass(Version.class);
		verify(versionManager).releaseVersion(releaseArgument.capture(), eq(true));

		final Version version = releaseArgument.getValue();
		assertThat(version.getName(), is(versionName));

		final ArgumentCaptor<Collection> reIndexArgument = ArgumentCaptor.forClass(Collection.class);
		verify(issueIndexManager).reIndexIssues(reIndexArgument.capture());

		final Collection<GenericValue> issues = reIndexArgument.getValue();
		assertThat(issues.size(), is(4));

		final Iterator<GenericValue> iterator = issues.iterator();
		assertThat(iterator.next().getString("key"), startsWith("DEV-12"));
		assertThat(iterator.next().getString("key"), startsWith("DEV-12"));
		assertThat(iterator.next().getString("key"), startsWith("DEV-12"));
		assertThat(iterator.next().getString("key"), startsWith("DEV-12"));
	}

	private MutableIssue mockedIssue(String issueKey, int statusId) {
		final MutableIssue issue = mock(MutableIssue.class, "Issue for issue key " + issueKey);
		when(issue.getKey()).thenReturn(issueKey);

		final com.atlassian.jira.project.Project project = mockedProject();
		when(issue.getProjectObject()).thenReturn(project);

		final GenericValue genericValue = mockedGenericValue(issueKey);
		when(issue.getGenericValue()).thenReturn(genericValue);
		
		final Status status = mockedStatus(statusId);
		when(issue.getStatusObject()).thenReturn(status);

		return issue;
	}

	private com.atlassian.jira.project.Project mockedProject() {
		final com.atlassian.jira.project.Project project = mock(com.atlassian.jira.project.Project.class);
		when(project.getId()).thenReturn(12L);

		return project;
	}

	private GenericValue mockedGenericValue(String issueKey) {
		final GenericValue genericValue = mock(GenericValue.class);
		when(genericValue.getString("key")).thenReturn(issueKey);

		return genericValue;
	}

	private Status mockedStatus(int statusId) {
		final Status status = mock(Status.class);
		when(status.getId()).thenReturn(String.valueOf(statusId));

		return status;
	}

	private Version mockedVersion(Long projectId, String versionName) {
		final Version version = mock(Version.class, "Version with name " + versionName);
		when(version.getId()).thenReturn(projectId);
		when(version.getName()).thenReturn(versionName);

		return version;
	}

	private DefaultVersionAssociationCreator createVac() {
		return new DefaultVersionAssociationCreator(versionManager, issueManager, fieldManager, cacheManager, issueIndexManager);
	}

	private Project createProject(final String versionForOkBuild) {
		final Project project = new Project("project", versionForOkBuild, createBuild(1));
		project.addFailedBuild(createBuild(2));
		project.addFailedBuild(createBuild(3));
		project.addFailedBuild(createBuild(6));

		return project;
	}

	private Build createBuild(final int counter) {
		return new Build(counter, createIssues(counter));
	}

	private Set<String> createIssues(final int counter) {
		return Sets.newHashSet("DEV-12" + counter, "DEV-23" + (counter + 1), "DEV-34" + (counter + 2));
	}

}
