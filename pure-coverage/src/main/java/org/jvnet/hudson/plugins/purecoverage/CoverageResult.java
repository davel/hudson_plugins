package org.jvnet.hudson.plugins.purecoverage;

import java.io.IOException;
import java.util.Collection;

import org.jvnet.hudson.plugins.purecoverage.domain.LineCoverage;
import org.jvnet.hudson.plugins.purecoverage.domain.LineCoverageMetric;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CoverageResult implements LineCoverageMetric {
	
	//Owner is necessary to render the sidepanel jelly
	private final Object owner;
	private final LineCoverageMetric metric;
	
	public CoverageResult(Object owner, LineCoverageMetric metric) {
		this.owner = owner;
		this.metric = metric;
	}

	public Object getOwner() {
		return this.owner;
	}
	
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException {
		return new CoverageResult(owner, this.getChild(token));
	}
	
	public boolean hasReport() {
		return metric != null;
	}

	//all below delegates to the metric:
	
	public LineCoverage getLineCoverage() {
		return metric.getLineCoverage();
	}
	
	public Collection<LineCoverageMetric> getChildren() {
		return metric.getChildren();
	}

	public LineCoverageMetric getChild(String childName) {
		return metric.getChild(childName);
	}	
	
	public String getChildMetricName() {
		return metric.getChildMetricName();
	}
	
	public String getMetricName() {
		return metric.getMetricName();
	}

	public String getUrlName() {
		return metric.getUrlName();
	}
}