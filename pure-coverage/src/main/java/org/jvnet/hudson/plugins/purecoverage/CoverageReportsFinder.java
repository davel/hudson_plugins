package org.jvnet.hudson.plugins.purecoverage;

import hudson.model.AbstractBuild;

import java.io.File;
import java.io.FilenameFilter;

@SuppressWarnings("unchecked")
public class CoverageReportsFinder {

	public static final String COVERAGE_PREFIX = "pure-coverage-data";

	public File[] findReports(AbstractBuild build) {
		return build.getRootDir().listFiles(new CoverageFilenameFilter());
	}
	
    private static class CoverageFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.startsWith(COVERAGE_PREFIX);
        }
    }
}