package org.jvnet.hudson.plugins.purecoverage;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@SuppressWarnings("unchecked")
public class CoverageProjectAction extends Actionable implements
		ProminentProjectAction {
	
	private static final long serialVersionUID = 1L;

	private AbstractProject owner;

	public CoverageProjectAction(AbstractProject owner) {
		this.owner = owner;
	}

	public String getDisplayName() {
		return "PureCoverage report";
	}

	public String getIconFileName() {
		return "graph.gif";
	}

	public String getUrlName() {
		return "purecoverage";
	}

	public String getSearchUrl() {
		return getUrlName();
	}

	public CoverageResult getLastCoverageResult() {
		Run build = owner.getLastStableBuild();
		if (build != null) {
			CoverageBuildAction action = build.getAction(CoverageBuildAction.class);
			return action.getCoverageResult();
        } else {
        	return null;
        }
	}
	
	public String getLastCoverageTotal() {
		Run build = owner.getLastStableBuild();
		if (build != null) {
			CoverageBuildAction action = build.getAction(CoverageBuildAction.class);
			return action.getCoverageTotal();
		} else {
			return null;
		}
	}
	
    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
    	if (hasResult()) {
    		rsp.sendRedirect2("../lastStableBuild/coverage");
    	} else {
    		rsp.sendRedirect2("nocoverage");
    	}
    	//We might redirect to some nodata document, but let's assume there's always last build with coverage stuff...
    }

	public boolean hasResult() {
		return getLastCoverageTotal() != null;
	}
}