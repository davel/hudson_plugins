package hudson.plugins.stagingrelease;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.xml.sax.InputSource;

/**
 * @plexus.component role="org.apache.maven.shared.release.phase.ReleasePhase"
 *                   role-hint="subversion-version"
 * @author awpyv
 * 
 */
public class SubversionVersionPhase extends AbstractReleasePhase {

	public ReleaseResult execute(ReleaseDescriptor releaseDescriptor,
			Settings settings, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {
		ReleaseResult result = new ReleaseResult();

		try {
			MavenProject rootProject = null;
			for (MavenProject project : (List<MavenProject>) reactorProjects) {
				if (project.getBasedir().getAbsolutePath().equals(
						releaseDescriptor.getWorkingDirectory())) {
					rootProject = project;
				}
			}
			String baseVersion = releaseDescriptor.getBaseVersion();
			if (baseVersion.contains("SVNDATE")) {
				String date = getDate(releaseDescriptor.getWorkingDirectory());
				baseVersion = baseVersion.replace("SVNDATE", date);
			}

			for (MavenProject project : (List<MavenProject>) reactorProjects) {
				String projectId = ArtifactUtils.versionlessKey(project
						.getArtifact());
				releaseDescriptor.mapReleaseVersion(projectId, baseVersion);
			}

			result.setResultCode(ReleaseResult.SUCCESS);
			return result;
		} catch (Exception e) {
			throw new ReleaseExecutionException("Error while finding versions",
					e);
		}

	}

	public ReleaseResult simulate(ReleaseDescriptor releaseDescriptor,
			Settings settings, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {
		return execute(releaseDescriptor, settings, reactorProjects);
	}

	private String getDate(String file)
			throws XPathExpressionException, CommandLineException {
		// fix case problem on Windows
		file = file.substring(0, 1).toUpperCase().concat(file.substring(1));
		
		Commandline cl = new Commandline("svn");
		cl.addArguments(new String[] { "info", "--xml" });
		cl.addArguments(new String[] { file });
		StringStreamConsumer consumer = new StringStreamConsumer();
		CommandLineUtils.executeCommandLine(cl, consumer,
				new StringStreamConsumer());
		String svnInfo = consumer.getOutput();

		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		String date = xpath.evaluate("/info//entry[@path='" + file
				+ "']/commit/date/text()", new InputSource(new StringReader(
				svnInfo)));
		date = date.replaceAll("-", "");
		date = date.replaceAll("T", "_");
		date = date.replaceAll(":", "");
		date = date.substring(0, 13);
		return date;
	}
}
