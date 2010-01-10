package edu.ucsc.soe.sep;

import com.google.common.base.Preconditions;
import hudson.model.AbstractProject;
import hudson.model.Action;

/**
 * User: cflewis
 * Date: Jan 9, 2010
 * Time: 5:01:09 PM
 */
public class SepProjectAction implements Action {
    AbstractProject<?,?> project;
    SepRecorder parentRecorder;

	public SepProjectAction(AbstractProject<?,?> project, SepRecorder parentRecorder) {
		this.project = Preconditions.checkNotNull(project);
        this.parentRecorder = Preconditions.checkNotNull(parentRecorder);
	}

	public AbstractProject<?,?> getProject() {
		return project;
	}

    public String getIconFileName() {
        return "clipboard.gif";
    }

    public String getDisplayName() {
        return "Sep";
    }

    public String getUrlName() {
        return "sep";
    }

    public String getUrl() {
        return parentRecorder.getUrl();
    }
}
