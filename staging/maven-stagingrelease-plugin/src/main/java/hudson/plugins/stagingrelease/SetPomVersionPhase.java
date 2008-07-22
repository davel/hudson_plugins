package hudson.plugins.stagingrelease;

import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.phase.AbstractRewritePomsPhase;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * 
 * @plexus.component role="org.apache.maven.shared.release.phase.ReleasePhase"
 *                   role-hint="set-pom-version"
 */
public class SetPomVersionPhase extends AbstractRewritePomsPhase {

	@Override
	public ReleaseResult execute(ReleaseDescriptor releaseDescriptor,
			Settings settings, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {

		String version = releaseDescriptor.getBaseVersion();

		for (MavenProject project : (List<MavenProject>) reactorProjects) {
			String projectId = ArtifactUtils.versionlessKey(project
					.getArtifact());
			ArtifactHandler artifactHandler = project.getArtifact()
					.getArtifactHandler();

			releaseDescriptor.mapReleaseVersion(projectId, project.getVersion());
			releaseDescriptor.mapDevelopmentVersion(projectId, version);
		}

		return super.execute(releaseDescriptor, settings, reactorProjects);
	}

	@Override
	protected void transformScm(MavenProject project, Element rootElement,
			Namespace namespace, ReleaseDescriptor releaseDescriptor,
			String projectId, ScmRepository scmRepository,
			ReleaseResult result, MavenProject rootProject)
			throws ReleaseExecutionException {
	}

	protected Map getOriginalVersionMap(ReleaseDescriptor releaseDescriptor,
			List reactorProjects) {
		return releaseDescriptor.getReleaseVersions();
	}

	protected Map getNextVersionMap(ReleaseDescriptor releaseDescriptor) {
		return releaseDescriptor.getDevelopmentVersions();
	}

	protected String getResolvedSnapshotVersion(String artifactVersionlessKey,
			Map resolvedSnapshotsMap) {
		Map versionsMap = (Map) resolvedSnapshotsMap
				.get(artifactVersionlessKey);

		if (versionsMap != null) {
			return (String) (versionsMap.get(ReleaseDescriptor.DEVELOPMENT_KEY));
		} else {
			return null;
		}
	}
}
