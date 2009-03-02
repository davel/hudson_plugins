package hudson.plugins.kagemai;

import static org.apache.commons.lang.StringUtils.isEmpty;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.plugins.kagemai.model.KagemaiIssue;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author yamkazu
 * 
 */
public class KagemaiChangelogAnnotator extends ChangeLogAnnotator {

	@Override
	public void annotate(AbstractBuild<?, ?> build, Entry change,
			MarkupText text) {
		KagemaiProjectProperty kagemaiProjectProperty = build.getParent()
				.getProperty(KagemaiProjectProperty.class);
		if (kagemaiProjectProperty == null) {
			return;
		}
		if (kagemaiProjectProperty.getSite() == null) {
			return;
		}
		if (kagemaiProjectProperty.isLinkEnabled() == false) {
			return;
		}

		String regex = kagemaiProjectProperty.getRegex();
		Pattern pattern = Pattern.compile(regex);
		HashSet<Integer> bugIds = new HashSet<Integer>();
		for (SubText token : text.findTokens(pattern)) {
			try {
				bugIds.add(getId(token));
			} catch (NumberFormatException e) {
				continue;
			}
		}
		if (bugIds.size() == 0) {
			return;
		}

		KagemaiBuildAction action = build.getAction(KagemaiBuildAction.class);
		if (action == null) {
			action = new KagemaiBuildAction(build, kagemaiProjectProperty
					.getKagemaiSession().getIssuesMap(bugIds),
					kagemaiProjectProperty.getSite().getBaseUrl()
							.toExternalForm(), kagemaiProjectProperty
							.getProjectId());
		}

		List<KagemaiIssue> issues = action.getIssues();
		for (SubText token : text.findTokens(pattern)) {
			Integer key = null;
			try {
				key = getId(token);
			} catch (Exception e) {
				continue;
			}
			String summary = null;
			for (KagemaiIssue issue : issues) {
				if (key == issue.getId()) {
					summary = issue.getSummary();
				}
			}
			if (isEmpty(summary)) {
				token.surroundWith(String.format(KagemaiSession.LINK_FORMAT,
						action.getSiteName(), action.getProjectId(), key),
						"</a>");
			} else {
				token.surroundWith(String.format(
						KagemaiSession.LINK_FORMAT_WITH_TOOLTIP, action
								.getSiteName(), action.getProjectId(), key,
						summary), "</a>");
			}
		}
	}

	private static int getId(SubText token) {
		String id = null;
		for (int i = 0;; i++) {
			id = token.group(i);
			try {
				return Integer.valueOf(id);
			} catch (NumberFormatException e) {
				continue;
			}
		}
	}

}
