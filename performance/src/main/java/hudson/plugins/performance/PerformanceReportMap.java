package hudson.plugins.performance;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class PerformanceReportMap implements ModelObject {

	private transient PerformanceBuildAction buildAction;

	private static final String PERFORMANCE_REPORTS_DIRECTORY = "performance-reports";

	private static final Logger logger = Logger.getLogger(PerformanceReportMap.class.getName());

	private Map<String, PerformanceReport> performanceReportMap = new HashMap<String, PerformanceReport>();

	PerformanceReportMap() {
	}

	PerformanceReportMap(PerformanceBuildAction buildAction) {
		this.buildAction = buildAction;
	}

	PerformanceReportMap(PerformanceBuildAction buildAction, List<File> pFileList) throws IOException {
		this.buildAction = buildAction;
		for (File pFile : pFileList) {
			performanceReportMap.put(pFile.getName(), new PerformanceReport(buildAction, pFile));
		}
	}

	public AbstractBuild<?, ?> getBuild() {
		return buildAction.getBuild();
	}

	PerformanceBuildAction getBuildAction() {
		return buildAction;
	}

	public String getDisplayName() {
		return "Performance";
	}

	public List<PerformanceReport> getPerformanceListOrdered() {
		Collection<PerformanceReport> uriCollection = getPerformanceReportMap().values();
		List<PerformanceReport> listPerformance = new ArrayList<PerformanceReport>(uriCollection.size());
		for (PerformanceReport performanceReport : uriCollection) {
			listPerformance.add(performanceReport);
		}
		Collections.sort(listPerformance);
		return listPerformance;
	}

	public Map<String, PerformanceReport> getPerformanceReportMap() {
		return performanceReportMap;
	}

	/**
	 * <p>
	 * Give the Performance report with the parameter for name in Bean
	 * </p>
	 * 
	 * @param performanceReportName
	 * @return
	 */
	public PerformanceReport getPerformanceReport(String performanceReportName) {
		PerformanceReport meterReport = null;
		if ((performanceReportMap == null) || (performanceReportMap.get(performanceReportName) == null)
				|| (performanceReportMap.get(performanceReportName) == null)) {
			File reportFile = new File(getBuild().getRootDir(), getPerformanceReportFileRelativePath(performanceReportName));
			try {
				meterReport = new PerformanceReport(buildAction, reportFile);
				if (meterReport.size() == 0) {
					logger.warn("Performance report analysis is empty, ensure your jtl file is filled with samples.");
				}
				if (performanceReportMap == null) {
					performanceReportMap = new HashMap<String, PerformanceReport>();
				}
				performanceReportMap.put(performanceReportName, meterReport);
			} catch (IOException e) {
				logger.warn("Failed to load " + reportFile, e);
				Throwable ex = e;
				do {
					logger.warn(ex.getLocalizedMessage());
					ex = ex.getCause();
				} while (ex != null);
			}
		} else {
			meterReport = performanceReportMap.get(performanceReportName);
		}
		return meterReport;
	}

	/**
	 * Get a URI report within a Performance report file
	 * 
	 * @param uriReport
	 *            "Performance report file name";"URI name"
	 * @return
	 */
	public UriReport getUriReport(String uriReport) {
		if (uriReport != null) {
			String uriReportDecoded;
			try {
				uriReportDecoded = URLDecoder.decode(uriReport.replace(UriReport.END_PERFORMANCE_PARAMETER, ""), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
			StringTokenizer st = new StringTokenizer(uriReportDecoded, GraphConfigurationDetail.SEPARATOR);
			return getPerformanceReportMap().get(st.nextToken()).getUriReportMap().get(st.nextToken());
		} else {
			return null;
		}
	}

	public String getUrlName() {
		return "performanceReportList";
	}

	void setBuildAction(PerformanceBuildAction buildAction) {
		this.buildAction = buildAction;
	}

	public void setPerformanceReportMap(Map<String, PerformanceReport> performanceReportMap) {
		this.performanceReportMap = performanceReportMap;
	}

	public static String getPerformanceReportFileRelativePath(String reportFileName) {
		return getRelativePath(reportFileName);
	}

	public static String getPerformanceReportDirRelativePath() {
		return getRelativePath(null);
	}

	private static String getRelativePath(String reportFileName) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(PERFORMANCE_REPORTS_DIRECTORY);
		if (reportFileName != null) {
			sb.append("/").append(reportFileName);
		}
		return sb.toString();
	}

	/**
	 * <p>
	 * Verify if the PerformanceReport exist the performanceReportName must to be like it
	 * is in the build
	 * </p>
	 * 
	 * @param performanceReportName
	 * @return boolean
	 */
	public boolean isFailed(String performanceReportName) {
		return getPerformanceReport(performanceReportName) == null;
	}
}
