package hudson.plugins.m2_repo_reaper;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Wraps an M2 project to clean some or all of the repository before starting.
 * In this version of the plugin, it will only do anything if the project
 * has a private M2 repository.
 * 
 * @author Benson Margulies
 * @version 1.0
 */
public class M2RepoReaperWrapper extends BuildWrapper {
	private String artifactPatterns;

	private List<String> patterns = new ArrayList<String>();

	@DataBoundConstructor
	public M2RepoReaperWrapper(final String artifactPatterns) {
		this.artifactPatterns = artifactPatterns;
	}

	public List<String> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<String> patterns) {
		this.patterns = patterns;
	}

	public String getArtifactPatterns() {
		return artifactPatterns;
	}

	public void setArtifactPatterns(String artifactPatterns) {
		this.artifactPatterns = artifactPatterns;
	}

	@Override
	public Environment setUp(AbstractBuild build, final Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		MavenModuleSet moduleSet;
		try {
			moduleSet = (MavenModuleSet) build.getProject();
		} catch (ClassCastException e) {
			return null;
		}

		if (!moduleSet.usesPrivateRepository()) {
			return null;
		}

		String repoPath = build.getWorkspace().child(".repository").getRemote();

		if (!doReap(repoPath, patterns, listener)) {
			throw new IOException("Could not execute pre-build steps");
		}

		// return environment
		return new Environment() {
		};
	}

	private boolean doReap(String repoPath, List<String> patterns,
			BuildListener listener) throws InterruptedException, IOException {
		
		File repo = new File(repoPath);
		if (!repo.exists()) {
			// this can happen on the first build.
			return true;
		}

		FilePath filePath = new FilePath(repo);
		Set<FilePath> totalList = new HashSet<FilePath>();
		for (String pattern : patterns) {
			FilePath[] paths = filePath.list(pattern);
			for (FilePath fp : paths) {
				totalList.add(fp);
			}
		}
		for (FilePath path : totalList) {
			listener.getLogger().append("Deleting " + path.absolutize() + "\n");
			try {
				path.deleteRecursive();
			} catch (IOException e) {
				listener.error("Failed to delete " + path.absolutize() + "\n");
			}
		}

		return true;

	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		@Override
		public String getDisplayName() {
			return "Configure M2 Repository Cleaning";
		}

		@Override
		public BuildWrapper newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			M2RepoReaperWrapper instance = req.bindJSON(
					M2RepoReaperWrapper.class, formData);
			List<String> patterns = new ArrayList<String>();
			for (String p : instance.getArtifactPatterns().split(",")) {
				patterns.add(p);
			}
			instance.setPatterns(patterns);
     		return instance;
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return (item instanceof AbstractMavenProject);
		}

		@Override
		public String getHelpFile() {
			return "/plugin/m2-repo-reaper/help-projectConfig.html";
		}
	}

}
