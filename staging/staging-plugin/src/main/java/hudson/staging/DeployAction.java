package hudson.staging;

import hudson.maven.EmbedderLoggerImpl;
import hudson.maven.MavenEmbedder;
import hudson.maven.MavenUtil;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Result;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.embedder.PlexusLoggerAdapter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.LargeText;

public class DeployAction implements Action {

	private static Logger LOGGER = Logger.getLogger(DeployAction.class
			.getName());

	private final AbstractBuild<?, ?> build;
	private final String version;
	private Thread worker;

	private String repositoryId;
	private String repositoryUrl;

	public DeployAction(AbstractBuild<?, ?> build, String version, String repositoryId, String repositoryUrl) {
		this.build = build;
		this.version = version;
		this.repositoryId = repositoryId;
		this.repositoryUrl = repositoryUrl;
	}

	public String getDisplayName() {
		return "Upload";
	}

	public String getIconFileName() {
		return "package.gif";
	}

	public String getUrlName() {
		return "upload";
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public void doDeploy(StaplerRequest req, StaplerResponse rsp)
			throws ServletException, IOException {
		req.bindParameters(this);
		worker = new Thread(new Runnable() {

			public void run() {
				deploy();
			}
		});
		worker.start();
		req.getView(this, "index").forward(req, rsp);
	}
	
	public void doCancel(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
		cancel();
		req.getView(this, "index").forward(req, rsp);
	}

	public Result getResult() {
		return result;
	}

	private Result result;

	public File getLogFile() {
		return new File(build.getRootDir(), "staging-upload.log");
	}

	public void cancel() {
		if (worker != null) {
			worker.interrupt();
		}
	}
	
	public void deploy() {
		StreamTaskListener listener = null;
		try {
			long start = System.currentTimeMillis();
			listener = new StreamTaskListener(getLogFile());

			try {
				MavenEmbedder embedder = MavenUtil.createEmbedder(listener,
						null);
				embedder.setInteractiveMode(false);
				try {
					ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) embedder
							.getContainer().lookup(
									ArtifactRepositoryLayout.ROLE, "default");
					ArtifactRepositoryFactory factory = (ArtifactRepositoryFactory) embedder
							.lookup(ArtifactRepositoryFactory.ROLE);

					ArtifactRepository targetRepository = factory
							.createDeploymentArtifactRepository(repositoryId,
									repositoryUrl, layout, false);
					ArtifactRepository sourceRepository = factory
							.createDeploymentArtifactRepository("source",
									new File(build.getRootDir(), "staging")
											.toURI().toURL().toExternalForm(),
									layout, false);

					WagonManager wagonManager = (WagonManager) embedder
							.lookup(WagonManager.ROLE);
					SCPRepositoryCopier copier = new SCPRepositoryCopier();
					copier.setWagonManager(wagonManager);
					copier.enableLogging(new PlexusLoggerAdapter(embedder.getLogger()));
					copier.copy((DefaultArtifactRepository) sourceRepository,
							(DefaultArtifactRepository) targetRepository,
							version);
					
					listener.getLogger().println("Upload completed in " + DurationFormatUtils.formatDurationWords(System.currentTimeMillis()-start, true,true));

				} finally {
					if (embedder != null)
						embedder.stop();
				}
				result = Result.SUCCESS;
			} catch (Exception e) {
				e.printStackTrace(listener.error(e.getMessage()));
				listener.getLogger().println("Upload stopped after " + DurationFormatUtils.formatDurationWords(System.currentTimeMillis()-start, true,true));
				result = Result.ABORTED;
			}

			worker = null;
			build.save();
		} catch (IOException e) {
			result = Result.FAILURE;
			LOGGER.log(Level.SEVERE, "Failed to write " + getLogFile(), e);
		} finally {
			if (listener != null)
				listener.close();
		}
	}

	/**
	 * Handles incremental log output.
	 */
	public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp)
			throws IOException {
		new LargeText(getLogFile(), !isRunning()).doProgressText(req, rsp);
	}

	/**
	 * Is this task still running?
	 */
	public boolean isRunning() {
		return worker != null;
	}
}
