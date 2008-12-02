package org.jvnet.hudson.plugins.purecoverage;

import java.io.File;
import java.io.IOException;

import org.jvnet.hudson.plugins.purecoverage.domain.ProjectCoverage;

public interface CoverageParser {

	/**
	 * @param coverageReport - input coverage report
	 * @param sourcePaths
	 */
	ProjectCoverage parse(File coverageReport) throws IOException;

}