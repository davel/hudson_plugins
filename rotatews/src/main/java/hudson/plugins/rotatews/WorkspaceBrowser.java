package hudson.plugins.rotatews;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildBadgeAction;
import hudson.model.DirectoryBrowserSupport;

public class WorkspaceBrowser implements BuildBadgeAction {
	AbstractBuild<?, ?> parent;
	
	FilePath buildWorkspace;
	
	public WorkspaceBrowser(AbstractBuild<?, ?> parent, FilePath ws) {
		this.parent = parent;
		this.buildWorkspace = ws;
	}
	
	public AbstractBuild<?, ?> getParent() {
		return parent;
	}
	
	public AbstractBuild<?, ?> getOwner() {
		return parent;
	}
	
	public boolean isAvailable() {
		try {
			return buildWorkspace.exists();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getDisplayName() {
		return "Workspace";
	}

	public String getIconFileName() {
		return (isAvailable() ? "folder.gif" : null);
	}

	public String getUrlName() {
		return "ws";
	}

    /**
     * Serves the workspace files.
     */
    public void doDynamic( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException {
        parent.checkPermission(AbstractProject.WORKSPACE);
        FilePath ws = buildWorkspace;
        if ((ws == null) || (!ws.exists())) {
            // if there's no workspace, report a nice error message
            req.getView(this,"noWorkspace.jelly").forward(req,rsp);
        } else {
            new DirectoryBrowserSupport(parent,getDisplayName()+" workspace").serveFile(req, rsp, ws, "folder.gif", true);
        }
    }

}
