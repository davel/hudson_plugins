package hudson.staging;

import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.MavenEmbedder;
import hudson.maven.MavenUtil;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class StagingPublisher extends Publisher {

	private final String repositoryLocation;
	private final String repositoryId;
	private final String repositoryUrl;

	@DataBoundConstructor
	public StagingPublisher(String repositoryLocation, String repositoryId, String repositoryUrl) {
		this.repositoryLocation = repositoryLocation;
		this.repositoryId = repositoryId;
		this.repositoryUrl = repositoryUrl;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public String getRepositoryLocation() {
		return repositoryLocation;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		FilePath target = new FilePath(new File(build.getRootDir(), "staging"));
		int num = build.getProject().getWorkspace().child(repositoryLocation)
				.copyRecursiveTo("**", target);
		
		String version = getVersion(build);
		if (version == null) { 
			version = Long.toString(System.currentTimeMillis());
		}
		
		if (num == 0) {
			listener.error("StagingPublisher: no files found to copy");
		} else {
			build.addAction(new BrowseAction(build));
			build.addAction(new DeployAction(build, version, repositoryId, repositoryUrl));
		}

		return true;
	}

	private String getVersion(AbstractBuild<?, ?> build)
			throws FileNotFoundException, IOException {

		Pattern pattern = Pattern.compile(".*\\[INFO\\] Uploading project information for "
				+ "[^\\s]* ([^\\s]*)");
		// Assume default encoding and text files
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(build
				.getLogFile()));
		while ((line = reader.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	public static final class DescriptorImpl extends Descriptor<Publisher> {

		private DescriptorImpl() {
			super(StagingPublisher.class);
		}

		@Override
		public String getDisplayName() {
			return "Publish staging repository";
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return req.bindJSON(StagingPublisher.class, formData);
		}

	}

	public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}
}
