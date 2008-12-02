package org.jvnet.hudson.plugins.purecoverage;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hudson.plugins.purecoverage.domain.ProjectCoverage;
import org.jvnet.hudson.plugins.purecoverage.parser.PureCoverageParser;
import org.kohsuke.stapler.StaplerProxy;

@SuppressWarnings("unchecked")
public class CoverageBuildAction implements StaplerProxy, Action {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CoverageBuildAction.class.getName());
	
	private final AbstractBuild owner;
	
	private transient WeakReference<CoverageResult> coverageResult;
	
	private String coverageTotal;

	public CoverageBuildAction(AbstractBuild owner, CoverageResult coverageResult) {
		setCoverageResult(coverageResult);
		if (owner == null) {
			throw new RuntimeException("owner cannot be null");
		}
		this.owner = owner;
	}

	private void setCoverageResult(CoverageResult coverageResult) {
		this.coverageResult = new WeakReference(coverageResult);
		this.coverageTotal = coverageResult.getLineCoverage().toString();
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

	public Object getTarget() {
		return getCoverageResult();
	}

	public CoverageResult getCoverageResult() {
		if (!hasResult()) {
			//try to reload from file
			reloadReport();
		}
		
		if (!hasResult()) {
			//return empty result
			return new CoverageResult(owner, null);
		}
		
		return coverageResult.get();
	}

	private boolean hasResult() {
		return coverageResult != null && coverageResult.get() != null;
	}

	private void reloadReport() {
		CoverageReportsFinder finder = new CoverageReportsFinder();
		File[] reports = finder.findReports(owner);
		logger.log(Level.INFO, "Reloading PureCoverage report from file(s): " + Arrays.toString(reports));
		for (File report : reports) {
		    try {
		    	CoverageParser coverageParser = new PureCoverageParser();
				ProjectCoverage projectCoverage = coverageParser.parse(report);
				CoverageResult result = new CoverageResult(owner, projectCoverage);
				setCoverageResult(result);
		    } catch (IOException e) {
		    	logger.log(Level.WARNING, "Unable to load coverage report from file: " + report, e);
		    }
		}
	}
	
	public String getCoverageTotal() {
		return coverageTotal;
	}
}