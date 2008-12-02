package org.jvnet.hudson.plugins.purecoverage.domain;


public class FunctionCoverage extends LineCoverageBase implements LineCoverageMetric {

	public FunctionCoverage(String functionName, int coveredLines, int totalLines) {
		this(functionName, new LineCoverage(coveredLines, totalLines));
	}

	public FunctionCoverage(String functionName, LineCoverage c) {
		super(functionName, c);
	}

	public String getChildMetricName() {
		return null;
	}
}