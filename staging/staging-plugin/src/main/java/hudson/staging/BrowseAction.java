package hudson.staging;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class BrowseAction implements Action {

	private AbstractBuild<?, ?> build;

	public BrowseAction(AbstractBuild<?, ?> build) {
		this.build = build;
	}

	public String getDisplayName() {
		return "Staging Repository";
	}

	public String getIconFileName() {
		return "folder.gif";
	}

	public String getUrlName() {
		return "staging";
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public void doDynamic(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException, InterruptedException {
		build.checkPermission(AbstractProject.WORKSPACE);
		FilePath ws = new FilePath(new File(build.getRootDir(), "staging"));
		if ((ws == null) || (!ws.exists())) {
			rsp.sendError(404);
		} else {
			new DirectoryBrowserSupport(build, getDisplayName()).serveFile(req,
					rsp, ws, "folder.gif", true);
		}
	}

//	public void doDynamic(StaplerRequest req, StaplerResponse rsp)
//			throws IOException, ServletException, InterruptedException {
//		build.checkPermission(AbstractProject.WORKSPACE);
//		FilePath ws = new FilePath(new File(build.getRootDir(), "staging"));
//		new DirectoryBrowserSupport(build, getDisplayName()).serveFile(
//				req, rsp, ws, "folder.gif", true);
//	}

}
