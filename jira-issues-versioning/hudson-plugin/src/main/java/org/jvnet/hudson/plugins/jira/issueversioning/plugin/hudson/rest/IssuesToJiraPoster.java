package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.rest;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run.Artifact;
import hudson.plugins.jira.JiraBuildAction;
import hudson.plugins.jira.JiraCarryOverAction;
import hudson.plugins.jira.JiraIssue;
import hudson.scm.ChangeLogSet.Entry;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Build;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils.JiraKeyUtils;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils.ProjectUtils;

/**
 * Posts issues in the current build and any previous failed builds to the specified Jira url.
 *
 * @author Stig Kleppe-Jorgensen, 2010.01.05
 */
public class IssuesToJiraPoster {
	private static final Pattern pattern = Pattern.compile(".*-([0-9.]+).jar$");

	private final AbstractBuild build;
	private String baseUri;
	private final PrintStream logger;

	public IssuesToJiraPoster(AbstractBuild build, String baseUri, PrintStream logger) throws MalformedURLException {
		this.build = build;
		this.baseUri = baseUri;
		this.logger = logger;
	}

	/**
	 * Posts the issues wrapped inside its project and its builds to Jira.
	 *
	 * @return true if the URL to post to is ok, false otherwise.
	 */
	public boolean post() throws JAXBException, IOException {
		Project project = createProject(build);
		final String marshaledProject = marshal(project);
		int status = postToJira(marshaledProject);

		final boolean postOk = status < 300;

		if (postOk) {
			logger.println("Posted the following " + numIssues(project) + " issues to Jira that is added to version " +
					project.getVersionForOkBuild() + " if status is fixed:");
			logger.println(project.getAllIssues());
		} else {
			logger.println("ERROR: posting to " + baseUri + " failed. Make sure the Jira URL is setup correctly");
		}

		return postOk;
	}

	private Project createProject(AbstractBuild<?, ?> build) {
		final String version = lookupVersionFromArtifacts(build.getArtifacts());
		final Project project = new Project(build.getProject().getName(), version, createBuild(build));

		AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();

		while (hasFailedOrNotNull(previousBuild)) {
			project.addFailedBuild(createBuild(previousBuild));
			previousBuild = previousBuild.getPreviousBuild();
		}

		return project;
	}

	private String lookupVersionFromArtifacts(List<? extends Artifact> artifacts) {
		for (Artifact artifact : artifacts) {
			final Matcher matcher = pattern.matcher(artifact.getFileName());

			if (matcher.matches()) {
				return matcher.group(1);
			}
		}

		throw new IllegalStateException("Could not find version information in generated artifacts. " +
				"Does this build produce Maven type artifacts?");
	}

	private Build createBuild(AbstractBuild<?, ?> build) {
		return new Build(build.getNumber(), findCurrentBuildIssues(build));
	}

	private Set<String> findCurrentBuildIssues(AbstractBuild<?, ?> build) {
		Set<String> issueKeys = new HashSet<String>();

		final JiraBuildAction jiraBuildAction = build.getAction(JiraBuildAction.class);
		final JiraIssue[] issues = jiraBuildAction.issues;

		final JiraCarryOverAction jiraCarryOverAction = build.getAction(JiraCarryOverAction.class);
		final Collection<String> issueIds = jiraCarryOverAction.getIDs();

		if (ProjectUtils.getJiraProjectKeyPropertyOfProject(build.getProject()) != null) {
			Pattern pattern = ProjectUtils.getJiraProjectKeyPropertyOfProject(build.getProject()).getIssueKeyPattern();

			for (Entry entry : build.getChangeSet()) {
				issueKeys.addAll(JiraKeyUtils.getJiraIssueKeysFromText(entry.getMsg(), pattern));
			}
		}

		return issueKeys;
	}

	private Set<String> findCarryOverIssues(AbstractBuild<?, ?> build) {
		Set<String> issueKeys = new HashSet<String>();

		final JiraBuildAction jiraBuildAction = build.getAction(JiraBuildAction.class);
		final JiraIssue[] issues = jiraBuildAction.issues;

		final JiraCarryOverAction jiraCarryOverAction = build.getAction(JiraCarryOverAction.class);
		final Collection<String> issueIds = jiraCarryOverAction.getIDs();

		if (ProjectUtils.getJiraProjectKeyPropertyOfProject(build.getProject()) != null) {
			Pattern pattern = ProjectUtils.getJiraProjectKeyPropertyOfProject(build.getProject()).getIssueKeyPattern();

			for (Entry entry : build.getChangeSet()) {
				issueKeys.addAll(JiraKeyUtils.getJiraIssueKeysFromText(entry.getMsg(), pattern));
			}
		}

		return issueKeys;
	}

	private boolean hasFailedOrNotNull(AbstractBuild<?, ?> previousBuild) {
		if (previousBuild == null) {
			return false;
		}

		final Result result = previousBuild.getResult();

		return result.isWorseOrEqualTo(Result.FAILURE);
	}

	private String marshal(Project project) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(new Class<?>[]{Project.class, Build.class});
		final Marshaller marshaller = jc.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(project, writer);

		return writer.toString();
	}

	private int postToJira(String marshaledProject) throws IOException {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(baseUri);
		post.setRequestEntity(new StringRequestEntity(marshaledProject, MediaType.APPLICATION_XML, null));

		return client.executeMethod(post);
	}

	private int numIssues(Project project) {
		int numIssues = project.getOkBuild().getIssues().size();

		for (Build failedBuild : project.getFailedBuilds()) {
			numIssues += failedBuild.getIssues().size();
		}

		return numIssues;
	}
}
