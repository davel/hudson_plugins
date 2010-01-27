package org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

/**
 * @author Stig Kleppe-Jorgensen, 2009.12.28
 * @fixme add description
 */
@XmlRootElement
public final class Project {
	@XmlElement
	private final String name;
	@XmlElement
	private String versionForOkBuild;
	@XmlElement
	private final Build okBuild;
	@XmlElement
	private final Set<Build> failedBuilds = new HashSet<Build>();
	@XmlTransient
	private final Set<String> allIssues;

	/**
	 * JAXB need default constructor to work.
	 */
	private Project() {
		name = "";
		versionForOkBuild = "";
		okBuild = Build.EMPTY;
		allIssues = new HashSet<String>();
	}

	public Project(String name, String versionForOkBuild, Build okBuild) {
		this.name = name;
		this.versionForOkBuild = versionForOkBuild;
		this.okBuild = okBuild;
		allIssues = new HashSet<String>();

		loadAllIssues();
	}

	public String getName() {
		return name;
	}

	public String getVersionForOkBuild() {
		return versionForOkBuild;
	}

	public Build getOkBuild() {
		return okBuild;
	}

	public void addFailedBuild(Build build) {
		failedBuilds.add(build);
		addToAllIssuesFrom(build);
	}

	public Set<Build> getFailedBuilds() {
		return Collections.unmodifiableSet(failedBuilds);
	}

	public Set<String> getAllIssues() {
		return allIssues;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Project project = (Project) o;

		return name.equals(project.name) &&
				versionForOkBuild.equals(project.versionForOkBuild) &&
				okBuild.equals(project.okBuild) &&
				failedBuilds.equals(project.failedBuilds);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + versionForOkBuild.hashCode();
		result = 31 * result + okBuild.hashCode();
		result = 31 * result + failedBuilds.hashCode();

		return result;
	}

	private void loadAllIssues() {
		Preconditions.checkNotNull(allIssues);

		addToAllIssuesFrom(okBuild);

		for (Build build : failedBuilds) {
			addToAllIssuesFrom(build);
		}
	}

	private void addToAllIssuesFrom(Build build) {
		allIssues.addAll(build.getIssues());
	}

	/**
	 * Called after unmarshalling is done so the list of all issues can be initialized.
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		loadAllIssues();
	}
}
