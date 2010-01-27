package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run.Artifact;
import hudson.plugins.jira.JiraBuildAction;
import hudson.plugins.jira.JiraCarryOverAction;
import hudson.plugins.jira.JiraIssue;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.test.EmbeddedContainer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Build;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.JiraProjectKeyJobProperty;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link IssuesToJiraPoster}
 *
 * @author Stig Kleppe-Jorgensen, 2010.01.05
 */
public class IssuesToJiraPosterTest {
	private static final Result[] results =
			new Result[]{Result.UNSTABLE, Result.FAILURE, Result.NOT_BUILT, Result.ABORTED};
	private static final ArgumentCaptor<Project> argument = ArgumentCaptor.forClass(Project.class);
	private final PrintStream printStream = mockPrintStream();

	@Test
	public void should_marshall() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(new Class<?>[]{Project.class, Build.class});
		final Marshaller marshaller = jc.createMarshaller();

		Project p = createProject();
		final StringWriter writer = new StringWriter();

		marshaller.marshal(p, writer);

		System.out.println(writer.toString());
	}

	private Project createProject() {
		final Project project = new Project("name", "1.2", new Build(2, Sets.newHashSet("DEV-123", "DEV-234")));
		project.addFailedBuild(new Build(3, Sets.newHashSet("DEV-345", "DEV-456")));

		return project;
	}


	@Test
	public void should_post_whole_build_issue_structure_to_given_url() throws MalformedURLException {
		postToUrlForBuild("", mockedBuildWithPreviousBuilds());

		final Project project = argument.getValue();
		assertThat(project.getName(), is("project-name"));
		assertThat(project.getVersionForOkBuild(), is("1.2.5"));
		assertThat(project.getOkBuild(), is(notNullValue()));
		assertThat(project.getFailedBuilds().size(), is(3));
		assertThat(project.getAllIssues().size(), is(2));

		assertThat(project.getOkBuild().getNumber(), is(5));
		assertThat(project.getOkBuild().getIssues().size(), is(2));

		assertThat(Iterables.get(project.getFailedBuilds(), 0).getNumber(), is(2));
		assertThat(Iterables.get(project.getFailedBuilds(), 0).getIssues().size(), is(2));
		assertThat(Iterables.get(project.getFailedBuilds(), 1).getNumber(), is(4));
		assertThat(Iterables.get(project.getFailedBuilds(), 1).getIssues().size(), is(2));
		assertThat(Iterables.get(project.getFailedBuilds(), 2).getNumber(), is(3));
		assertThat(Iterables.get(project.getFailedBuilds(), 2).getIssues().size(), is(2));

		verify(printStream).println(
				"Posted the following 8 issues to Jira that is added to version 1.2.5 if status is fixed:");
		verify(printStream).println(Sets.newHashSet("DEV-1234", "DEV-4312"));
	}

	@Test
	public void should_return_false_for_wrong_url() throws MalformedURLException {
		boolean correctUrl = postToUrlForBuild("/should_return_404", mockedBuildWithPreviousBuilds());

		assertThat(correctUrl, is(false));
		verify(printStream).println("ERROR: posting to http://localhost:8081/should_return_404 failed. " +
				"Make sure the Jira URL is setup correctly");
	}

	@Test
	public void should_stop_searching_when_no_more_previous_builds() throws MalformedURLException {
		final AbstractBuild build = mockedBuildWith(mockProject());
		when(build.getPreviousBuild()).thenReturn(null);

		postToUrlForBuild("", build);

		final Project project = argument.getValue();
		assertThat(project.getOkBuild(), is(notNullValue()));
		assertThat(project.getFailedBuilds().size(), is(0));
	}

	private boolean postToUrlForBuild(final String path, final AbstractBuild build) throws MalformedURLException {
		final String baseUrl = TestPortProvider.generateURL(path);
		final IssuesToJiraPoster poster = new IssuesToJiraPoster(build, baseUrl, printStream);

		try {
			return poster.post();
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private PrintStream mockPrintStream() {
		return mock(PrintStream.class);
	}

	private AbstractBuild mockedBuildWithPreviousBuilds() {
		final AbstractProject project = mockProject();
		final AbstractBuild build = mockedBuildWith(project);

		mockPreviousBuild(build, project);

		return build;
	}

	private AbstractBuild mockedBuildWith(AbstractProject project) {
		final AbstractBuild build = mock(AbstractBuild.class);

		when(build.getProject()).thenReturn(project);
		when(build.getNumber()).thenReturn(5);
		when(build.getAction(JiraBuildAction.class)).thenReturn(createJiraBuildAction());
		final List<Artifact> mockedArtifacts = mockedArtifacts();
		when(build.getArtifacts()).thenReturn(mockedArtifacts);

		return build;
	}

	private JiraBuildAction createJiraBuildAction() {
		return new JiraBuildAction(null, createIssues());
	}

	private Collection<JiraIssue> createIssues() {
		final List<JiraIssue> issues = new ArrayList<JiraIssue>();
		issues.add(new JiraIssue("DEV-1234", "test issue DEV-1234"));
		issues.add(new JiraIssue("DEV-4312", "test issue DEV-4312"));

		return issues;
	}

	private List<Artifact> mockedArtifacts() {
		final Artifact artifact = mock(Artifact.class);
		when(artifact.getFileName()).thenReturn("test-andre-1.2.5.jar");

		return Collections.singletonList(artifact);
	}

	private void mockPreviousBuild(AbstractBuild build, AbstractProject project) {
		AbstractBuild previousBuild = mockGetPrevious(build);

		when(previousBuild.getProject()).thenReturn(project);
		when(previousBuild.getNumber()).thenReturn(4);
		when(previousBuild.getAction(JiraCarryOverAction.class)).thenReturn(createJiraCarryOverAction());
	}

	private JiraCarryOverAction createJiraCarryOverAction() {
		return new JiraCarryOverAction(createIssueIds());
	}

	private Collection<String> createIssueIds() {
		final List<String> issueIds = new ArrayList<String>();
		issueIds.add("DEV-1234");
		issueIds.add("DEV-5432");

		return issueIds;
	}

	private AbstractBuild mockGetPrevious(AbstractBuild build) {
		AbstractBuild previousBuild = mock(AbstractBuild.class);
		when(build.getPreviousBuild()).thenReturn(previousBuild);

		return previousBuild;
	}

	private AbstractProject mockProject() {
		final AbstractProject project = mock(AbstractProject.class);
		when(project.getName()).thenReturn("project-name");
		final JiraProjectKeyJobProperty property = mockJiraProjectKeyJobProperty();
		when(project.getProperty(JiraProjectKeyJobProperty.class)).thenReturn(property);

		return project;
	}

	private JiraProjectKeyJobProperty mockJiraProjectKeyJobProperty() {
		final JiraProjectKeyJobProperty property = mock(JiraProjectKeyJobProperty.class);
		when(property.getJiraBaseUrl()).thenReturn("test-url");

		return property;
	}

	@BeforeClass
	public static void startupServer() throws Exception {
		Dispatcher dispatcher = EmbeddedContainer.start().getDispatcher();
		dispatcher.getRegistry().addSingletonResource(mockedVersionResource());
	}

	@AfterClass
	public static void stopContainer() throws Exception {
		EmbeddedContainer.stop();
	}

	private static VersionResource mockedVersionResource() {
		final VersionResource versionResource = mock(VersionResource.class);
		when(versionResource.associateWithIssues(argument.capture())).thenReturn(Response.ok().build());

		return versionResource;
	}

	/**
	 * REST resource that associates the given issues for a release build in Hudson with a JIRA version. The version name
	 * is taken from the Maven version of the release.
	 *
	 * @author Stig Kleppe-Jorgensen, 2009.12.29
	 */
	@Path("/version")
	public static interface VersionResource {
		@POST
		@Path("/associate")
		@AnonymousAllowed
		@Consumes(MediaType.APPLICATION_XML)
		Response associateWithIssues(Project project);
	}
}
