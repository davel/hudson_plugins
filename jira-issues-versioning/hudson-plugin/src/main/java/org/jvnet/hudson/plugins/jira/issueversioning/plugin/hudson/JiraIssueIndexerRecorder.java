package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.plugins.jira.JiraCarryOverAction;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.rest.IssuesToJiraPoster;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils.ProjectUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Jira Issue Indexer {@link Publisher}
 *
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
// FIXME should this be a recorder? Are there something else that is run post build with a better name?
@SuppressWarnings("unchecked")
public class JiraIssueIndexerRecorder extends Recorder {
	private final boolean postIssues;

	/**
	 * Constructor
	 */
	@DataBoundConstructor
	public JiraIssueIndexerRecorder(boolean postIssues) {
		this.postIssues = postIssues;
	}

	public boolean isPostIssues() {
		return postIssues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		final PrintStream logger = listener.getLogger();
		JiraCarryOverAction a = build.getPreviousBuild().getAction(JiraCarryOverAction.class);

		return true;
	}

	private void postBuild(AbstractBuild<?, ?> build, PrintStream logger) {
		final JiraProjectKeyJobProperty jobProperty =
				ProjectUtils.getJiraProjectKeyPropertyOfProject(build.getProject());
		final String jiraUrl = jobProperty.getJiraBaseUrl();

		logger.println("Posting issues to Jira...");
		try {
			final IssuesToJiraPoster poster = new IssuesToJiraPoster(build, jiraUrl, logger);
			poster.post();
		} catch (MalformedURLException e) {
			logger.println("ERROR: problems posting to " + jiraUrl + ". Is it correct?");
		} catch (JAXBException e) {
			logger.println("ERROR: problems with marshalling: " + e);
		} catch (IOException e) {
			logger.println("ERROR: problems posting to " + jiraUrl + ". Is it correct?");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/**
	 * {@link Descriptor} for {@link JiraIssueIndexerRecorder}
	 *
	 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
	 */
	@Extension
	public static final class JiraIssueIndexerRecorderDescriptor extends BuildStepDescriptor<Publisher> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getDisplayName() {
			return Messages.getJiraIssueIndexerDisplayName();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getHelpFile() {
			return "/plugin/hudson-jiraapi-plugin/help-indexer.html";
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(JiraIssueIndexerRecorder.class,formData);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}
	}
}
