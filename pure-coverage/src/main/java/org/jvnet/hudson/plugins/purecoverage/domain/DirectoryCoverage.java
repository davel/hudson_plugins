package org.jvnet.hudson.plugins.purecoverage.domain;

public class DirectoryCoverage extends LineCoverageBase implements LineCoverageMetric {

	public DirectoryCoverage(String directoryName, int coveredLines, int totalLines) {
		this(directoryName, new LineCoverage(coveredLines, totalLines));
	}
	
	public DirectoryCoverage(String directoryName, LineCoverage lineCoverage) {
		super(directoryName, lineCoverage);
	}

	public String getChildMetricName() {
		return "File";
	}
}