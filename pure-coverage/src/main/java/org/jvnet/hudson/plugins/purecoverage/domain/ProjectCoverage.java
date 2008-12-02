package org.jvnet.hudson.plugins.purecoverage.domain;

public class ProjectCoverage extends LineCoverageBase implements LineCoverageMetric {

	public ProjectCoverage(int coveredLines, int totalLines) {
		this(new LineCoverage(coveredLines, totalLines));
	}
	
	public ProjectCoverage(LineCoverage c) {
		super("Project total", c);
	}

	public String getChildMetricName() {
		return "Directory";
	}
}