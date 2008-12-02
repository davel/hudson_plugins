package org.jvnet.hudson.plugins.purecoverage.domain;

import java.util.Collection;

public interface LineCoverageMetric {

	String getUrlName();
	
	LineCoverage getLineCoverage();
	
	Collection<LineCoverageMetric> getChildren();

	LineCoverageMetric getChild(String childUrlName);
	
	String getChildMetricName();
	
	String getMetricName();
	
}