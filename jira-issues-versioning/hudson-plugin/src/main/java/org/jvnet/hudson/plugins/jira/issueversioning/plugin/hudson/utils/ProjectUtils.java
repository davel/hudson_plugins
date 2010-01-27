package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hudson.matrix.MatrixConfiguration;
import hudson.maven.MavenModule;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.ItemGroup;
import org.codehaus.plexus.util.StringUtils;
import org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.JiraProjectKeyJobProperty;

/**
 * Helper class for Hudson Jobs
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
public class ProjectUtils {

	/**
	 * Get all the Hudson Projects
	 * 
	 * @return {@link Set} of {@link AbstractProject} objects
	 */
	@SuppressWarnings("unchecked")
	public static Set<AbstractProject<?, ?>> getAllProjects() {
		final Set<AbstractProject<?, ?>> supported = new HashSet<AbstractProject<?, ?>>();
		final List<AbstractProject> projects = Hudson.getInstance().getAllItems(AbstractProject.class);
		for (AbstractProject<?, ?> project : projects) {
			if (isSupportedProjectType(project)) {
				supported.add(project);
			}
		}
		return supported;
	}

	/**
	 * Get all the Hudson Projects
	 * 
	 * @return {@link Set} of {@link AbstractProject} objects
	 */
	@SuppressWarnings("unchecked")
	public static Set<AbstractProject<?, ?>> getAllProjectsIncludingModules() {
		final Set<AbstractProject<?, ?>> supported = new HashSet<AbstractProject<?, ?>>();
		final List<AbstractProject> projects = Hudson.getInstance().getAllItems(AbstractProject.class);
		for (AbstractProject<?, ?> project : projects) {
			supported.add(project);
		}
		return supported;
	}

	/**
	 * Get the Hudson Project by Jira Project Key
	 * 
	 * @param key the Jira project key
	 * @return the {@link AbstractProject}, may be <code>null</code> if no {@link AbstractProject} can be found
	 */
	public static AbstractProject<?, ?> getProjectByJiraProjectKey(final String key) {
		final Set<AbstractProject<?, ?>> projects = getAllProjectsIncludingModules();
		for (AbstractProject<?, ?> project : projects) {
			if (project.getProperty(JiraProjectKeyJobProperty.class) != null) {
				final JiraProjectKeyJobProperty jiraProperty =
					(JiraProjectKeyJobProperty) project.getProperty(JiraProjectKeyJobProperty.class);
				if (key.equals(jiraProperty.getKey())) {
					if (isSupportedProjectType(project)) {
						return project;
					} else {
						return (AbstractProject<?, ?>) project.getParent();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get a Hudson project by name
	 * 
	 * @param projectName the name of the project
	 * @param parentName the name of the project parent, may be <code>null</code> if the project doesn't have a
	 *            parent, required if looking up a subproject like MavenModule
	 * @return the {@link AbstractProject} object
	 */
	public static AbstractProject<?, ?> getProjectByName(String projectName, String parentName) {
		if (StringUtils.isEmpty(parentName)) {
			return (AbstractProject<?, ?>) Hudson.getInstance().getItem(projectName);
		} else {
			final AbstractProject<?, ?> parent = (AbstractProject<?, ?>) Hudson.getInstance().getItem(parentName);
			return (AbstractProject<?, ?>) ((ItemGroup<?>) parent).getItem(projectName);
		}
	}

	/**
	 * Check if the Jira integration supports the project
	 * 
	 * @param <PROJECT> the Type of the project
	 * @param project the {@link AbstractProject} project to check
	 * @return <code>true</code> if supported, <code>false</code> otherwise
	 */
	public static <PROJECT extends AbstractProject<?, ?>> boolean isSupportedProjectType(PROJECT project) {
		return (!(project instanceof MavenModule) && !(project instanceof MatrixConfiguration));
	}

	/**
	 * Get all the builds of a project
	 * 
	 * @param <PROJECT> the Type of project
	 * @param project the project to get all the builds for
	 * @return {@link List} of Builds
	 */
	@SuppressWarnings("unchecked")
	public static <PROJECT extends AbstractProject<?, ?>> List<AbstractBuild<?, ?>> getProjectModuleBuilds(
					PROJECT project) {
		final List<AbstractBuild<?, ?>> builds = new ArrayList<AbstractBuild<?, ?>>();
		if (project instanceof ItemGroup) {
			final Collection<PROJECT> modules = ((ItemGroup<PROJECT>) project).getItems();
			for (PROJECT module : modules) {
				builds.addAll(module.getBuilds());
			}
		}
		return builds;
	}

	/**
	 * Get all the builds of a {@link List} of projects
	 * 
	 * @param <PROJECT> the Type of project
	 * @param projects the {@link List} of projects to get all the builds for
	 * @return {@link List} of Builds
	 */
	public static <PROJECT extends AbstractProject<?, ?>> List<AbstractBuild<?, ?>> getProjectModuleBuilds(
					Collection<PROJECT> projects) {
		final List<AbstractBuild<?, ?>> builds = new ArrayList<AbstractBuild<?, ?>>();
		for (PROJECT project : projects) {
			builds.addAll(getProjectModuleBuilds(project));
		}
		return builds;
	}

	/**
	 * Get the {@link JiraProjectKeyJobProperty} of a given Hudson Project
	 * 
	 * @param <PROJECT> the Project type
	 * @param project the Hudson project
	 * @return the {@link JiraProjectKeyJobProperty} property of the project if any is configured, may be
	 *         <code>null</code>
	 */
	public static <PROJECT extends AbstractProject<?, ?>> JiraProjectKeyJobProperty getJiraProjectKeyPropertyOfProject(
					PROJECT project) {
		if (project.getProperty(JiraProjectKeyJobProperty.class) != null) {
			return (JiraProjectKeyJobProperty) project.getProperty(JiraProjectKeyJobProperty.class);
		}
		return null;
	}

}
