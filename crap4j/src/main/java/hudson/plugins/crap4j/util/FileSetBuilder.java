package hudson.plugins.crap4j.util;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.PatternSet.NameEntry;

public class FileSetBuilder {
	
	private final File workspaceRoot;

	public FileSetBuilder(File workspaceRoot) {
		super();
		this.workspaceRoot = workspaceRoot;
	}
	
	public FileSet createFileSetFor(String pattern) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(this.workspaceRoot);
    	PatternSet patternSet = fileSet.createPatternSet();
        StringTokenizer tokenizer = new StringTokenizer(pattern, ",");
        while (tokenizer.hasMoreTokens()) {
        	NameEntry include = patternSet.createInclude();
        	include.setName(tokenizer.nextToken());
        }
        return fileSet;
	}
}
