package hudson.plugins.stagingrelease;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.stage.RepositoryCopier;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @plexus.component role="org.apache.maven.shared.release.phase.ReleasePhase"
 *                   role-hint="deploy-repository"
 * @author awpyv
 * 
 */
public class DeployRepositoryPhase extends AbstractReleasePhase {

	private ArtifactRepository localRepository;
	private ArtifactRepository stagingRepository;
	private ArtifactRepository deploymentRepository;

	/**
	 * @plexus.requirement
	 */
	private RepositoryCopier repositoryCopier;

	/**
	 * @todo replace this with a real deployment that also works remote.
	 */
	public ReleaseResult execute(ReleaseDescriptor releaseDescriptor,
			Settings settings, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {
		ReleaseResult result = new ReleaseResult();
		
		if (!releaseDescriptor.isDeploy()) {
			result.setResultCode(ReleaseResult.UNDEFINED);
			return result;
		}

		try {
			if (releaseDescriptor.getDeploymentRepository().contains("scp:")) {
				deployRemote(releaseDescriptor);
			} else {
				deployLocal(releaseDescriptor);
			}

			result.setResultCode(ReleaseResult.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			logError(result, "Error while releasing.");
			result.setResultCode(ReleaseResult.ERROR);
			throw new ReleaseExecutionException("Error while releasing", e);
		}
		return result;

	}

	private void deployRemote(ReleaseDescriptor releaseDescriptor)
			throws MojoFailureException, WagonException, IOException {
		String version = (String) releaseDescriptor.getReleaseVersions()
				.values().iterator().next();
		DefaultArtifactRepository deploymentRepository = RepositoryUtil
				.createRepository(releaseDescriptor.getDeploymentRepository());
		DefaultArtifactRepository stagingRepository = RepositoryUtil
				.createRepository(releaseDescriptor
						.getTempDeploymentRepository());
		repositoryCopier.copy(stagingRepository, deploymentRepository, version);

	}

	private void deployLocal(ReleaseDescriptor releaseDescriptor)
			throws MalformedURLException, ArtifactDeploymentException,
			ReleaseExecutionException, MojoFailureException {

		deploymentRepository = RepositoryUtil
				.createRepository(releaseDescriptor.getDeploymentRepository());
		stagingRepository = RepositoryUtil.createRepository(releaseDescriptor
				.getTempDeploymentRepository());
		localRepository = new DefaultArtifactRepository("local", new File(
				releaseDescriptor.getLocalRepository()).toURL().toString(),
				new DefaultRepositoryLayout());

		for (Artifact a : getArtifacts(stagingRepository.getBasedir()))
			deployArtifact(a.getGroupId(), a.getArtifactId(), a.getVersion());
	}

	private List<Artifact> getArtifacts(String basedir) {
		DirectoryScanner ds = new DirectoryScanner();
		ds.setIncludes(new String[] { "**/*.pom" });
		ds.setBasedir(basedir);
		ds.scan();
		String[] includedFiles = ds.getIncludedFiles();
		List<Artifact> result = new ArrayList<Artifact>();
		for (String s : includedFiles) {
			s = s.substring(0, s.lastIndexOf(File.separatorChar));
			int i = s.lastIndexOf(File.separatorChar);
			String version = s.substring(i + 1);
			s = s.substring(0, i);
			i = s.lastIndexOf(File.separatorChar);
			String artifactId = s.substring(i + 1);
			String groupId = s.substring(0, i).replace(File.separatorChar, '.');
			Artifact artifact = artifactFactory.createArtifactWithClassifier(
					groupId, artifactId, version, "pom", null);
			result.add(artifact);
			System.out.printf("%s %s %s\n", groupId, artifactId, version);
		}
		return result;

	}

	public ReleaseResult simulate(ReleaseDescriptor releaseDescriptor,
			Settings settings, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {
		ReleaseResult result = new ReleaseResult();
		result.setResultCode(ReleaseResult.ERROR);
		return result;
	}

	/**
	 * @plexus.requirement
	 */
	private ArtifactFactory artifactFactory;

	/**
	 * @plexus.requirement
	 */
	private ArtifactDeployer artifactDeployer;

	public void deployArtifact(String groupId, String artifactId, String version)
			throws ArtifactDeploymentException, ReleaseExecutionException {

		Artifact pomArtifact = artifactFactory.createArtifactWithClassifier(
				groupId, artifactId, version, "pom", null);
		String basedir = ((Repository) stagingRepository).getBasedir();
		File pomFile = new File(basedir, stagingRepository.pathOf(pomArtifact));
		Model model = readModel(pomFile);

		Artifact artifact = artifactFactory.createArtifactWithClassifier(
				groupId, artifactId, version, model.getPackaging(), null);
		File file = new File(basedir, stagingRepository.pathOf(artifact));

		ArtifactMetadata metadata = new ProjectArtifactMetadata(artifact,
				pomFile);
		artifact.addMetadata(metadata);
		artifact.setRelease(true);
		artifactDeployer.deploy(file, artifact, deploymentRepository,
				localRepository);

		DirectoryScanner ds = new DirectoryScanner();
		ds.setBasedir(file.getParent());
		ds.setExcludes(new String[] { "*.md5", "*.sha1", pomFile.getName(),
				file.getName() });
		ds.scan();
		Pattern pattern = Pattern.compile(artifactId + "-" + version
				+ "-(.*)\\.(.*)");
		for (String s : ds.getIncludedFiles()) {
			Matcher m = pattern.matcher(s);
			if (!m.matches()) {
				throw new IllegalArgumentException("File does not match!");
			}
			String classifier = m.group(1);
			String type = m.group(2);
			Artifact attached = artifactFactory.createArtifactWithClassifier(
					groupId, artifactId, version, type, classifier);
			artifactDeployer.deploy(new File(file.getParent(), s), attached,
					deploymentRepository, localRepository);
		}

	}

	/**
	 * Extract the Model from the specified file.
	 * 
	 * @param pomFile
	 * @return
	 * @throws MojoExecutionException
	 *             if the file doesn't exist of cannot be read.
	 */
	protected Model readModel(File pomFile) throws ReleaseExecutionException {

		if (!pomFile.exists()) {
			throw new ReleaseExecutionException(
					"Specified pomFile does not exist");
		}

		Reader reader = null;
		try {
			reader = new FileReader(pomFile);
			MavenXpp3Reader modelReader = new MavenXpp3Reader();
			return modelReader.read(reader);
		} catch (FileNotFoundException e) {
			throw new ReleaseExecutionException(
					"Error reading specified POM file: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ReleaseExecutionException(
					"Error reading specified POM file: " + e.getMessage(), e);
		} catch (XmlPullParserException e) {
			throw new ReleaseExecutionException(
					"Error reading specified POM file: " + e.getMessage(), e);
		} finally {
			IOUtil.close(reader);
		}
	}

}
