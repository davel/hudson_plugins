package org.jvnet.hudson.plugins.purecoverage.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jvnet.hudson.plugins.purecoverage.util.UrlTransformer;

public class LineCoverageBase {

	private final String coverageElement;
	private LineCoverage lineCoverage;
	private final Map<String, LineCoverageMetric> childrenMap = new LinkedHashMap<String, LineCoverageMetric>();

	public LineCoverageBase(String coverageElement, int coveredLines, int totalLines) {
		this(coverageElement, new LineCoverage(coveredLines, totalLines));
	}

	public LineCoverageBase(String coverageElement, LineCoverage lineCoverage) {
		this.coverageElement = coverageElement;
		this.lineCoverage = lineCoverage;
	}
	
	public LineCoverageMetric getChild(String childName) {
		LineCoverageMetric coverage = childrenMap.get(childName);
		if (coverage == null) {
			throw new RuntimeException("Could not look up coverage for key: " + childName 
					+ "\nAll keys: " + childrenMap.keySet());
		}
		return coverage;
	}

	public Collection<LineCoverageMetric> getChildren() {
		return childrenMap.values();
	}
	
	public void addChild(LineCoverageMetric child) {
		this.childrenMap.put(child.getUrlName(), child);
	}

	public LineCoverage getLineCoverage() {
		return lineCoverage;
	}

	public String getMetricName() {
		return coverageElement;
	}

	public String getUrlName() {
		return new UrlTransformer().toUniqueUrl(coverageElement);
	}
	
	public String toString() {
		return coverageElement + ": " + lineCoverage;
	}
}
