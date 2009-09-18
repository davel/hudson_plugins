package hudson.plugins.jsunit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * Interface for mocking out the JSUnitReportTransformer from testing.
 */
public interface TestReportTransformer {
    /**
     * Transforms the jsUnit file stream to junit files in the specified output path
     * 
     * @param jsUnitFileStream jsUnit report file stream
     * @param junitOutputPath the output path to store junit reports to
     * @throws ParserConfigurationException 
     */
    void transform(InputStream jsUnitFileStream, File junitOutputPath) throws IOException, TransformerException,
            SAXException, ParserConfigurationException;
}
