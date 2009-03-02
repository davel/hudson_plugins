package hudson.plugins.kagemai;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.kagemai.model.KagemaiIssue;

import java.util.List;

/**
 * Kagemai issues related to the build.
 * 
 * @author yamkazu
 * 
 */
public class KagemaiBuildAction implements Action {

	private AbstractBuild<?, ?> owner;
	private List<KagemaiIssue> issues;
	private String siteName;
	private String projectId;

	public KagemaiBuildAction(AbstractBuild<?, ?> owner,
			List<KagemaiIssue> issues, String siteName, String projectId) {
		this.owner = owner;
		this.issues = issues;
		this.siteName = siteName;
		this.projectId = projectId;
	}

	public String getIssuesList() {

		KagemaiProjectProperty kagemaiProjectProperty = owner.getParent()
				.getProperty(KagemaiProjectProperty.class);
		if (kagemaiProjectProperty == null
				|| kagemaiProjectProperty.getSite() == null) {
			return "";
		}

		String baseUrl = kagemaiProjectProperty.getSite().getBaseUrl()
				.toExternalForm();
		String projectId = kagemaiProjectProperty.getProjectId();

		StringBuilder sb = new StringBuilder();
		for (KagemaiIssue issue : issues) {
			sb.append(String.format(new StringBuilder().append("<li>").append(
					KagemaiSession.LINK_FORMAT).append(issue.getId()).append(
					"</a>").append(" - ").append(issue.getSummary()).append(
					"</li>").append("\n").toString(), baseUrl, projectId, issue
					.getId()));
		}

		return sb.toString();
	}

	public List<KagemaiIssue> getIssues() {
		return issues;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getDisplayName() {
		return Messages.action_dispname();
	}

	public String getIconFileName() {
		return "/plugin/kagemai/images/kagemai.gif";
	}

	public String getUrlName() {
		return "kagemaiResult";
	}

	public AbstractBuild<?, ?> getOwner() {
		return this.owner;
	}

}
