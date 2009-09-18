package hudson.plugins.jsunit;

import hudson.plugins.jsunit.JSUnitReportTransformer;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Unit test for the XSL transformation
 * 
 * @author Erik Ramfelt
 */
public class JSUnitToJUnitXslTest {
    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void testTransformation() throws Exception {

        Transform myTransform = new Transform(new InputSource(this.getClass().getResourceAsStream("JSUnit-simple.xml")),
                new InputSource(this.getClass().getResourceAsStream(JSUnitReportTransformer.JSUNIT_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("JUnit-simple.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testTransformationFailure() throws Exception {

        Transform myTransform = new Transform(
                new InputSource(this.getClass().getResourceAsStream("JSUnit-failure.xml")), new InputSource(this
                        .getClass().getResourceAsStream(JSUnitReportTransformer.JSUNIT_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("JUnit-failure.xml"), myTransform.getResultString());
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    private String readXmlAsString(String resourceName) throws IOException {
        String xmlString = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
                resourceName)));
        String line = reader.readLine();
        while (line != null) {
            xmlString += line + "\n";
            line = reader.readLine();
        }
        reader.close();

        return xmlString;
    }
}
