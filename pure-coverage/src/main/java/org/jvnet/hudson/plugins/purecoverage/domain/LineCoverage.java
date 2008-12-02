package org.jvnet.hudson.plugins.purecoverage.domain;

public class LineCoverage {

	private final int coveredLines;
	private final int totalLines;
	private final String coverageString;

	public LineCoverage(int coveredLines, int totalLines) {
		this.coveredLines = coveredLines;
		this.totalLines = totalLines;
		double coverageValue = (totalLines == 0)? 0 : coveredLines / (1.0 * totalLines) * 100;
		String coverage = String.format("%.1f", coverageValue);
		this.coverageString = coverage + "% (" +  coveredLines + "/" + totalLines + ")";
	}
	
	public String toString() {
		return coverageString;
	}

	public int getTotalLines() {
		return totalLines;
	}
	
	public int getCoveredLines() {
		return coveredLines;
	}
}