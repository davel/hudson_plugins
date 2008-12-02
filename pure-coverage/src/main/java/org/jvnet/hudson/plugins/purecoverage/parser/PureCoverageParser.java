package org.jvnet.hudson.plugins.purecoverage.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.jvnet.hudson.plugins.purecoverage.CoverageParser;
import org.jvnet.hudson.plugins.purecoverage.domain.DirectoryCoverage;
import org.jvnet.hudson.plugins.purecoverage.domain.FileCoverage;
import org.jvnet.hudson.plugins.purecoverage.domain.FunctionCoverage;
import org.jvnet.hudson.plugins.purecoverage.domain.LineCoverage;
import org.jvnet.hudson.plugins.purecoverage.domain.ProjectCoverage;

public class PureCoverageParser implements CoverageParser {

	public ProjectCoverage parse(File coverageReport) throws FileNotFoundException {
		return parse(new FileReader(coverageReport));
	}
	
	public ProjectCoverage parse(Reader coverageReport) {
		BufferedReader br = new BufferedReader(coverageReport);
		
		ProjectCoverage projectCoverage = null;
		DirectoryCoverage directoryCoverage = null;
		FileCoverage fileCoverage = null;
		try {
			String line = null;
			while((line = br.readLine()) != null) {
				ParsingLine pLine = new ParsingLine(line);
				if (pLine.startsWith("to\t")) {
			        LineCoverage c = pLine.coverageFromCells(3, 4);
					projectCoverage = new ProjectCoverage(c);
				} else if (pLine.startsWith("di\t")) {
					String directoryName = pLine.stringAt(1);
					LineCoverage c = pLine.coverageFromCells(4, 5);
					directoryCoverage = new DirectoryCoverage(directoryName, c);
					projectCoverage.addChild(directoryCoverage);
				} else if (pLine.startsWith("fi\t")) {
			        String fileName = pLine.stringAt(1);
			        LineCoverage c = pLine.coverageFromCells(4, 5);
					fileCoverage = new FileCoverage(fileName, c);
					directoryCoverage.addChild(fileCoverage);
				} else if (pLine.startsWith("fu\t")) {
					String functionName = pLine.stringAt(1);
					LineCoverage c = pLine.coverageFromCells(4, 5);
					fileCoverage.addChild(new FunctionCoverage(functionName, c));
				}
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException("cannot parse file", e);
		}
		
		
		return projectCoverage;
	}
}