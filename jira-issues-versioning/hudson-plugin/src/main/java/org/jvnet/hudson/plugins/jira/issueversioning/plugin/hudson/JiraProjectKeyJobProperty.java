package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.codehaus.plexus.util.StringUtils;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils.JiraKeyUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * {@link JobProperty} to implement a property for a Jira Project Key
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
@ExportedBean
public class JiraProjectKeyJobProperty extends JobProperty<AbstractProject<?, ?>> implements
Comparable<JiraProjectKeyJobProperty> {

	private String key = "";

	/**
	 * Constructor
	 * 
	 * @param key the JIRA Key
	 */
	@DataBoundConstructor
	public JiraProjectKeyJobProperty(String key) {
		setKey(key);
	}

	/**
	 * Gets the JIRA Key
	 * 
	 * @return the JIRA Key
	 */
	@Exported(name = "jira-key")
	public String getKey() {
		return key;
	}
	
	/**
	 * Sets the JIRA Project Key
	 * 
	 * @param key the JIRA Project Key
	 */
	public void setKey(String key) {
		if (StringUtils.isEmpty(key)) {
			return;
		}
		if (JiraKeyUtils.isValidProjectKey(key, getProjectKeyPattern())) {
			this.key = key;
		} else {
			throw new IllegalArgumentException(key + " is not a valid JIRA Project Key ("
				+ getProjectKeyPattern().pattern() + ")");
		}
	}

	/**
	 * Get the Global Jira project key {@link Pattern}
	 * 
	 * @return the Global Jira project key {@link Pattern}
	 */
	public Pattern getProjectKeyPattern() {
		return ((JiraProjectKeyJobPropertyDescriptor) getDescriptor()).getProjectKeyPattern();
	}

	/**
	 * Get the Global Jira issue key {@link Pattern}
	 * 
	 * @return the Global Jira issue key {@link Pattern}
	 */
	public Pattern getIssueKeyPattern() {
		return ((JiraProjectKeyJobPropertyDescriptor) getDescriptor()).getIssueKeyPattern();
	}

	public String getJiraBaseUrl() {
		return ((JiraProjectKeyJobPropertyDescriptor) getDescriptor()).getJiraBaseUrl();
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(JiraProjectKeyJobProperty other) {
		return getKey().compareTo(other.getKey());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JiraProjectKeyJobProperty) {
			return getKey().equals(((JiraProjectKeyJobProperty) obj).getKey());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getKey();
	}

	/**
	 * {@link JobPropertyDescriptor} for {@link JiraProjectKeyJobProperty}
	 * 
	 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
	 */
	@Extension
	public static final class JiraProjectKeyJobPropertyDescriptor extends JobPropertyDescriptor {

		private Pattern projectKeyPattern = JiraKeyUtils.DEFAULT_JIRA_PROJECT_KEY_PATTERN;

		private Pattern issueKeyPattern = JiraKeyUtils.DEFAULT_JIRA_ISSUE_KEY_PATTERN;
		private String JiraBaseUrl;

		/**
		 * Constructor
		 */
		public JiraProjectKeyJobPropertyDescriptor() {
			super(JiraProjectKeyJobProperty.class);
			load();
		}

		/**
		 * {@inheritDoc}
		 */
		public String getDisplayName() {
			return Messages.JiraKeyProperty_DisplayName();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			try {
				if (StringUtils.isNotEmpty(json.getString("projectKeyPattern"))) {
					projectKeyPattern = Pattern.compile(json.getString("projectKeyPattern"));
				}
			} catch (PatternSyntaxException e) {
				throw new FormException(e, "Invalid Jira Project key pattern");
			}

			try {
				if (StringUtils.isNotEmpty(json.getString("issueKeyPattern"))) {
					issueKeyPattern = Pattern.compile(json.getString("issueKeyPattern"));
				}
			} catch (PatternSyntaxException e) {
				throw new FormException(e, "Invalid Jira Issue key pattern");
			}

			if (StringUtils.isNotEmpty(json.getString("jiraBaseUrl"))) {
				JiraBaseUrl = json.getString("jiraBaseUrl");
			}

			save();
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}

		/**
		 * Get the configured Jira project key pattern
		 * 
		 * @return the Jira project key pattern
		 */
		public Pattern getProjectKeyPattern() {
			return projectKeyPattern;
		}

		/**
		 * Get the configured Jira issue key pattern
		 * 
		 * @return the Jira issue key pattern
		 */
		public Pattern getIssueKeyPattern() {
			return issueKeyPattern;
		}

		public String getJiraBaseUrl() {
			return JiraBaseUrl;
		}
	}
}
