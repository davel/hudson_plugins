package hudson.plugins.crap4j.util;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;

public class ReportFilesFinder implements FileCallable<FoundFile[]> {

	private static final long serialVersionUID = -1666598324699232787L;
	private final boolean isSkippingOldFiles;
	private final String pattern;

	public ReportFilesFinder(String pattern) {
		this(pattern, false);
	}

	public ReportFilesFinder(String pattern, boolean isSkippingOldFiles) {
		super();
		this.pattern = pattern;
		this.isSkippingOldFiles = isSkippingOldFiles;
	}

	//@Override
	public FoundFile[] invoke(File workspaceRoot, VirtualChannel channel)
			throws IOException {
		return getFilesFor(workspaceRoot);
	}

    private String[] getRelativePaths(File workspaceRoot) {
    	FileSetBuilder builder = new FileSetBuilder(workspaceRoot);
    	FileSet fileSet = builder.createFileSetFor(this.pattern);
        return fileSet.getDirectoryScanner().getIncludedFiles();
    }

    private FoundFile[] getFoundFiles(File workspaceRoot) {
    	String[] relativePaths = getRelativePaths(workspaceRoot);
    	FoundFile[] result = new FoundFile[relativePaths.length];
    	for (int i = 0; i < result.length; i++) {
			result[i] = new FoundFile(
					new FilePath(new File(workspaceRoot, relativePaths[i])),
					relativePaths[i]);
		}
    	return result;
    }

    private boolean isAcceptable(FoundFile file) {
    	return true;
    }

	public FoundFile[] getFilesFor(File workspaceRoot) {
		FoundFile[] rawFindings = getFoundFiles(workspaceRoot);
		List<FoundFile> result = new ArrayList<FoundFile>();
		for (FoundFile foundFile : rawFindings) {
			if (isAcceptable(foundFile)) {
				result.add(foundFile);
			}
		}
		return result.toArray(new FoundFile[result.size()]);
		// TODO: Incorporate this code to exclude old, unreadable, empty or otherwise crappy report files

//        for (String file : findBugsFiles) {
//            File findbugsFile = new File(workspace, file);
//
//            String moduleName = guessModuleName(findbugsFile.getAbsolutePath());
//            MavenModule module = new MavenModule(moduleName);
//
//            if (SKIP_OLD_FILES && findbugsFile.lastModified() < buildTime) {
//                String message = "Skipping " + findbugsFile + " because it's not up to date";
//                logger.println(message);
//                module.setError(message);
//                continue;
//            }
//            if (!findbugsFile.canRead()) {
//                String message = "Skipping " + findbugsFile + " because we have no permission to read the file.";
//                logger.println(message);
//                module.setError(message);
//                continue;
//            }
//            if (new FilePath(findbugsFile).length() <= 0) {
//                String message = "Skipping " + findbugsFile + " because its empty.";
//                logger.println(message);
//                module.setError(message);
//                continue;
//            }
//
//            module = parseFile(workspace, findbugsFile, module);
//            project.addModule(module);
//        }
	}
}
