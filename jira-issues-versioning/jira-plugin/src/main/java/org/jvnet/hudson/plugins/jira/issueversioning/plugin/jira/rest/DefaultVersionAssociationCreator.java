package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.cache.CacheManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;
import org.ofbiz.core.entity.GenericValue;

// FIXME Must split issues on each project, like DEV-123, DEV-234, TUC-321 --> [DEV-123, DEV-234], [TUC-321]
public class DefaultVersionAssociationCreator implements VersionAssociationCreator {
	private final VersionManager versionManager;
	private final IssueManager issueManager;
	private final CacheManager cacheManager;
	private final IssueIndexManager indexManager;
	private final FixVersionsSystemField field;

	public DefaultVersionAssociationCreator(VersionManager versionManager, IssueManager issueManager,
	                                        FieldManager fieldManager, CacheManager cacheManager,
	                                        IssueIndexManager indexManager) {
		this.versionManager = versionManager;
		this.issueManager = issueManager;
		this.cacheManager = cacheManager;
		this.indexManager = indexManager;
		field = (FixVersionsSystemField) fieldManager.getField(IssueFieldConstants.FIX_FOR_VERSIONS);
	}

	/**
	 * {@inheritDoc}
	 */
	public void associateFor(Project project) {
		final Iterable<Issue> issues = transformIssueKeys(project.getAllIssues());
		final Iterable<Issue> filteredIssues = filterIssues(issues);

		if (filteredIssues.iterator().hasNext()) {
			final Issue issue = filteredIssues.iterator().next();
			
			try {
				Version version = fixVersion(issue, project.getVersionForOkBuild());
				addFixVersionToAll(filteredIssues, version);

				versionManager.releaseVersion(version, true);
			} catch (CreateException e) {
				throw new IllegalStateException("Could not create fix version for issue: " + issue, e);
			} catch (IndexException e) {
				throw new IllegalStateException("Could not reindex issues: " + filteredIssues, e);
			}
		}
	}

	private Iterable<Issue> transformIssueKeys(Set<String> issues) {
		return Iterables.transform(issues, new Function<String, Issue>() {
			public Issue apply(@Nullable String issueKey) {
				return issueManager.getIssueObject(issueKey);
			}
		});
	}

	/**
	 * Only include getIssues that is releasable (see {@link #isReleasable})
	 *
	 * @param issues getIssues to filter on
	 * @return a filtered view of the getIssues
	 */
	private Iterable<Issue> filterIssues(Iterable<Issue> issues) {
		return Iterables.filter(issues, new Predicate<Issue>() {
			public boolean apply(@Nullable Issue issue) {
				return isReleasable(issue);
			}
		});
	}

	/**
	 * A releasable issue is:
	 * <p/>
	 * <ul>
	 * <li> RESOLVED or CLOSED
	 * </ul>
	 * <p/>
	 *
	 * When only checking on status, there must exist a pre-commit check that only lets a check-in get through if the
	 * issue is not resolved or "higher".
	 *
	 * @param issue issue to check "releasability" on
	 * @return true if issue is releasable, false if not
	 */
	private boolean isReleasable(Issue issue) {
		final int status = Integer.parseInt(issue.getStatusObject().getId());

		// FIXME should a closed issue fail everything? Developer only set it to resolved and this plugin set it to
		// closed? Do we need a verification step for a released build?
		return status == IssueFieldConstants.RESOLVED_STATUS_ID ||
				status == IssueFieldConstants.CLOSED_STATUS_ID;
	}

	private Version fixVersion(Issue issue, String versionForOkBuild) throws CreateException {
		Version version = versionManager.getVersion(issue.getProjectObject().getId(), versionForOkBuild);

		if (version == null) {
			version = versionManager.
					createVersion(versionForOkBuild, new Date(), "", issue.getProjectObject().getId(), null);
		}

		return version;
	}

	private void addFixVersionToAll(Iterable<Issue> issues, Version version) throws IndexException {
		Collection<GenericValue> changedIssues = new ArrayList<GenericValue>();

		for (Issue issue : issues) {
			addFixVersionTo(issue, version);
			changedIssues.add(issue.getGenericValue());
		}

		if (!changedIssues.isEmpty()) {
			cacheManager.flush(CacheManager.ISSUE_CACHE, changedIssues);
			indexManager.reIndexIssues(changedIssues);
		}
	}

	private void addFixVersionTo(Issue issue, Version version) {
		final List<Version> versions = new ArrayList<Version>(issue.getFixVersions());
		versions.add(version);
		field.createValue(issue, versions);
	}
}