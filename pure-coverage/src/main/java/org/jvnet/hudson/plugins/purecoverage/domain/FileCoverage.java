package org.jvnet.hudson.plugins.purecoverage.domain;


public class FileCoverage extends LineCoverageBase implements LineCoverageMetric {

	public FileCoverage(String fileName, int coveredLines, int totalLines) {
		this(fileName, new LineCoverage(coveredLines, totalLines));
	}

	public FileCoverage(String fileName, LineCoverage c) {
		super(fileName, c);
	}

	public String getChildMetricName() {
		return "Function";
	}
}