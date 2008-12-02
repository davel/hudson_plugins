/**
 * 
 */
package org.jvnet.hudson.plugins.purecoverage.domain;

import java.util.Random;


/**
 * Used to generate dummy data
 */
public class ProjectCoverageFactory {
	
	private int nextTotal;
	private int nextCovered;

	public ProjectCoverage createRandomReport() {
		ProjectCoverage coverage = new ProjectCoverage(nextCovered(), nextTotal());
		
		DirectoryCoverage dir = new DirectoryCoverage("src/foo/bar", nextCovered(), nextTotal());
		dir.addChild(fileCoverage("File.c", nextCovered(), nextTotal()));
		dir.addChild(fileCoverage("OtherFile.h", nextCovered(), nextTotal()));
		coverage.addChild(dir);
		
		dir = new DirectoryCoverage("etl/src/foo/bar", nextCovered(), nextTotal());
		dir.addChild(fileCoverage("File.c", nextCovered(), nextTotal()));
		dir.addChild(fileCoverage("File2.c", nextCovered(), nextTotal()));
		dir.addChild(fileCoverage("FooBar.c", nextCovered(), nextTotal()));
		coverage.addChild(dir);
	
		dir = new DirectoryCoverage("src/bar", nextCovered(), nextTotal());
		dir.addChild(fileCoverage("File2.c", nextCovered(), nextTotal()));
		dir.addChild(fileCoverage("FooBar.c", nextCovered(), nextTotal()));
		coverage.addChild(dir);
		
		return coverage;
	}

	private FileCoverage fileCoverage(String file, int lc, int tl) {
		FileCoverage coverage = new FileCoverage(file, lc, tl);
		coverage.addChild(new FunctionCoverage("void foo()", 23, 991));
		coverage.addChild(new FunctionCoverage("void bar()", 45, 47));
		coverage.addChild(new FunctionCoverage("void baz()", 0, 34));
		return coverage;
	}

	private int nextCovered() {
		nextCovered = new Random().nextInt(100);
		return nextCovered;
	}

	private int nextTotal() {
		nextTotal = nextCovered + new Random().nextInt(100);
		return nextTotal;
	}
	
}