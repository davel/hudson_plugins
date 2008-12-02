package org.jvnet.hudson.plugins.purecoverage.parser;

import org.jvnet.hudson.plugins.purecoverage.domain.LineCoverage;

public class ParsingLine {

	private final String line;
	private final String[] splitLine;

	public ParsingLine(String line) {
		this.line = line;
		this.splitLine = line.split("\t");
	}

	public boolean startsWith(String string) {
		return line.startsWith(string);
	}

	public LineCoverage coverageFromCells(int x, int y) {
		int totalLines = Integer.parseInt(splitLine[y]);
		int coveredLines = totalLines - Integer.parseInt(splitLine[x]);
		return new LineCoverage(coveredLines, totalLines);
	}

	public String stringAt(int i) {
		return splitLine[i];
	}
}