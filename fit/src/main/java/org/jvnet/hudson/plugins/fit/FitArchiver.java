package org.jvnet.hudson.plugins.fit;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jvnet.hudson.plugins.fit.HtmlContentHandler.FitResult;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link FitArchiver} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #pathToHtml}) to remember the configuration.
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be invoked.
 * 
 * @author Eric Lefevre
 */
public class FitArchiver extends Recorder {
	private static final String PLUGIN_NAME = "fit";

	private static final String DOT_HTML = ".html";
	private static final String INDEX_HTML = "index" + DOT_HTML;
	private final String pathToHtml;

	FitArchiver(String pathToHtml) {
		this.pathToHtml = pathToHtml;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getPathToHtml() {
		return pathToHtml;
	}

	/**
	 * Gets the directory where the files will be archived.
	 */
	private static File getTargetDir(AbstractItem project) {
		return new File(project.getRootDir(), PLUGIN_NAME);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException {

		FilePath sourceDirectory = build.getWorkspace().child(
				pathToHtml);
		FilePath targetDirectory = new FilePath(getTargetDir(build.getParent()));

		try {
			// if the build has failed, then there's not much point in reporting
			// an error saying fit directory doesn't exist. We want the
			// user to focus on the real error, which is the build failure.
			if (build.getResult().isWorseOrEqualTo(Result.FAILURE)
					&& !sourceDirectory.exists()) {
				listener.getLogger().println(
						Messages.FitPlugin_CouldNotArchiveFitTests());
				return true;
			}

			listener
					.getLogger()
					.println(
							Messages
									.FitPlugin_DeletingContentOfArchiveDirectory(targetDirectory));
			targetDirectory.deleteContents();
			listener.getLogger().println(
					Messages.FitPlugin_CopyingFromSouceToTargetDirectory(
							sourceDirectory, targetDirectory));
			sourceDirectory.copyRecursiveTo("**/*", targetDirectory);

			FilePath indexHtml = targetDirectory.child(INDEX_HTML);
			if (indexHtml.exists()) {
				String msg = indexHtml.getName()
						+ " already present: no file will be generated";
				listener.getLogger().println(msg);
			} else {
				generateIndexHtml(listener, targetDirectory);
			}

		} catch (IOException e) {
			Util.displayIOException(e, listener);
			build.setResult(Result.FAILURE);
		}

		return true;
	}

	/**
	 * Generates an index file for the Fit results. It is necessary to have at
	 * least one index.html or the web server will show an error.
	 * <p>
	 * The index file will list all HTML files, and show the number of errors
	 * and failures.
	 */
	private void generateIndexHtml(BuildListener listener,
			FilePath reportDirectory) throws IOException, InterruptedException {
		FilePath[] htmlReports = reportDirectory.list("*" + DOT_HTML);
		String content = "";
		for (FilePath filePath : htmlReports) {
			listener.getLogger().println("Now parsing " + filePath.getRemote());
			FitResult fitResult = HtmlContentHandler.parse(new File(filePath
					.getRemote()));
			content += "<li>";
			content += "<a href='" + filePath.getName() + "'>";
			content += StringUtils.removeEnd(filePath.getName(), DOT_HTML);
			content += "</a> ";
//			content += fitResult.getErrorsNumber() + " "
//					+ getPlural(fitResult.getErrorsNumber(), "error", "errors")
//					+ ", ";
//			content += fitResult.getExpectationsNumber()
//					+ " "
//					+ getPlural(fitResult.getExpectationsNumber(), "failure",
//							"failures");
		}

		FilePath tempFile = reportDirectory.createTextTempFile("temp", "txt",
				content);
		FilePath indexHtml = reportDirectory.child(INDEX_HTML);
		tempFile.copyTo(indexHtml);
		listener.getLogger()
				.println(Messages.FitPlugin_IndexCreated(indexHtml));
	}

	private String getPlural(int number, String singular, String plural) {
		if (number <= 1) {
			return singular;
		} else {
			return plural;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Action getProjectAction(final AbstractProject<?, ?> project) {
		return new FitAction(project);
	}

	/**
	 * Descriptor for {@link FitArchiver}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
        @Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {
		public DescriptorImpl() {
			super(FitArchiver.class);
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return Messages.FitPlugin_PublisherDisplayName();
		}

		/** {@inheritDoc} */
		@Override
		public String getHelpFile() {
			return "/plugin/fit/help.html";
		}

		/**
		 * Creates a new instance of {@link FitArchiver} from a submitted form.
		 */
		@Override
		public FitArchiver newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			String param1FromJellyFile = "fit.pathToHtml";
			return new FitArchiver(req.getParameter(param1FromJellyFile));
		}

		/**
		 * Performs on-the-fly validation on the file mask wildcard.
		 */
		public FormValidation doCheck(@AncestorInPath AbstractProject project, @QueryParameter String value)
				throws IOException, ServletException {
			FilePath ws = project.getSomeWorkspace();
			return ws != null ? ws.validateRelativeDirectory(value) : FormValidation.ok();
		}

		@SuppressWarnings("unchecked")
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			// this option should be available whether the job is Maven or not
			return true;
		}

	}

	public static final class FitAction implements ProminentProjectAction {
		private static final String ICON_FILENAME = "orange-square.gif";
		private static final long serialVersionUID = 4399590075673857468L;
		private final AbstractItem project;

		public FitAction(AbstractItem project) {
			this.project = project;
		}

		public String getUrlName() {
			return PLUGIN_NAME;
		}

		public String getDisplayName() {
			return Messages.FitPlugin_ActionDisplayName();
		}

		public String getIconFileName() {
			if (getTargetDir(project).exists()) {
				return ICON_FILENAME;
			} else {
				// hide it since we don't have fit reports yet.
				return null;
			}
		}

		public DirectoryBrowserSupport doDynamic(StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException, InterruptedException {
			// handles the conversion of the URL into a proper local directory

			String title = project.getDisplayName() + " " + PLUGIN_NAME;
			FilePath systemDirectory = new FilePath(getTargetDir(project));
			return new DirectoryBrowserSupport(this, systemDirectory, title, ICON_FILENAME, false);
		}
	}

}
