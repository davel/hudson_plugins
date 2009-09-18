package hudson.plugins.jsunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.SAXException;

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

/**
 * Class responsible for transforming JSUnit to JUnit files and then run them all through the JUnit result archiver.
 * 
 * @author Erik Ramfelt
 */
public class JSUnitArchiver implements FilePath.FileCallable<Boolean>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String JUNIT_REPORTS_PATH = "temporary-junit-reports";

    // Build related objects
    private final BuildListener listener;
    private final String testResultsPattern;

    private TestReportTransformer unitReportTransformer;

    public JSUnitArchiver(BuildListener listener, String testResults, TestReportTransformer unitReportTransformer) throws TransformerException {
        this.listener = listener;
        this.testResultsPattern = testResults;
        this.unitReportTransformer = unitReportTransformer;
    }

    /** {@inheritDoc} */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        Boolean retValue = Boolean.TRUE;
        String[] jsUnitFiles = findJSUnitReports(ws);
        if (jsUnitFiles.length > 0) {
            File junitOutputPath = new File(ws, JUNIT_REPORTS_PATH);
            junitOutputPath.mkdirs();
    
            for (String jsUnitFileName : jsUnitFiles) {
                FileInputStream fileStream = new FileInputStream(new File(ws, jsUnitFileName));
                try {
                    unitReportTransformer.transform(fileStream, junitOutputPath);
                } catch (TransformerException te) {
                    throw new IOException2(
                            "Could not transform the JSUnit report. Please report this issue to the plugin author", te);
                } catch (SAXException se) {
                    throw new IOException2(
                            "Could not transform the JSUnit report. Please report this issue to the plugin author", se);
                } catch (ParserConfigurationException pce) {
                    throw new IOException2(
                            "Could not initalize the XML parser. Please report this issue to the plugin author", pce);
                } finally {
                    fileStream.close();
                }
            }
        } else {
            retValue = Boolean.FALSE;
        }

        return retValue;
    }

    /**
     * Return all JSUnit report files
     * 
     * @param parentPath parent
     * @return an array of strings
     */
    private String[] findJSUnitReports(File parentPath) {
        FileSet fs = Util.createFileSet(parentPath,testResultsPattern);
        DirectoryScanner ds = fs.getDirectoryScanner();

        String[] jsUnitFiles = ds.getIncludedFiles();
        if (jsUnitFiles.length == 0) {
            // no test result. Most likely a configuration error or fatal problem
            listener.fatalError("No JSUnit test report files were found. Configuration error?");
        }
        return jsUnitFiles;
    }
}
