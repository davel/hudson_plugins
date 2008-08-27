package hudson.plugins.jmeter;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.util.ArrayList;
import java.util.List;

public class UriReport implements ModelObject {

	private final JMeterReport jmeterReport;

	private final List<HttpSample> httpSampleList = new ArrayList<HttpSample>();

	private String uri;

	UriReport(JMeterReport jmeterReport, String uri) {
		this.jmeterReport = jmeterReport;
		setUri(uri);
	}

	public AbstractBuild getBuild() {
		return jmeterReport.getBuild();
	}

	public void addHttpSample(HttpSample httpSample) {
		httpSampleList.add(httpSample);
	}

	public int countErrors() {
		int nbError = 0;
		for (HttpSample currentSample : httpSampleList) {
			if (!currentSample.isSuccessful()) {
				nbError++;
			}
		}
		return nbError;
	}

	public long getAverage() {
		long average = 0;
		for (HttpSample currentSample : httpSampleList) {
			average += currentSample.getDuration();
		}
		return average / size();
	}

	public long getMax() {
		return httpSampleList.get(size() - 1).getDuration();
	}

	public long getMin() {
		return httpSampleList.get(0).getDuration();
	}

	public List<HttpSample> getHttpSampleList() {
		return httpSampleList;
	}

	public String getUri() {
		return uri;
	}

	public boolean isFailed() {
		return countErrors() != 0;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int size() {
		return httpSampleList.size();
	}

	public String getDisplayName() {
		return getUri();
	}
}
