package hudson.plugins.findbugs.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.test.AbstractEnglishLocaleTest;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.JavaPackage;
import hudson.plugins.analysis.util.model.LineRange;
import hudson.plugins.analysis.util.model.MavenModule;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class NativeFindBugsParserTest extends AbstractEnglishLocaleTest {
    /** Error message. */
    private static final String WRONG_MESSAGE_PARSED = "Wrong message parsed.";
    /** Error message. */
    private static final String BUG_WITH_GIVEN_HASHCODE_NOT_FOUND = "Bug with given hashcode not found";
    /** Hash code of second warning. */
    private static final String SECOND_WARNING = "f32497e4bd8c80ef6228f10bd3363f52";
    /** Hash code of first warning. */
    private static final String FIRST_WARNING = "4d839755cabf60eacc6438ac77ac5104";
    /** File in native format. */
    private static final String FINDBUGS_NATIVE_XML = "findbugs-native.xml";
    /** Number of warnings contained in files. */
    private static final int NUMBER_OF_WARNINGS = 2;
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_PARSED = "Wrong number of bugs parsed.";

    /**
     * Parses the specified file.
     *
     * @param fileName the file to read
     * @return the parsed module
     * @throws IOException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    private MavenModule parseFile(final String fileName) throws IOException, DocumentException {
        Collection<FileAnnotation> annotations = new NativeFindBugsParser().parse(NativeFindBugsParserTest.class.getResourceAsStream(fileName), new ArrayList<String>(), fileName, new HashMap<String, String>());
        MavenModule module = new MavenModule(fileName);
        if (!annotations.isEmpty()) {
            module.setName(annotations.iterator().next().getModuleName());
        }
        module.addAnnotations(annotations);

        return module;
    }

    /**
     * Tests the message mapping.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     */
    @Test
    public void testMessageMapping() throws SAXException, IOException {
        Map<String, String> mapping = new NativeFindBugsParser().createHashToMessageMapping(NativeFindBugsParserTest.class.getResourceAsStream(FINDBUGS_NATIVE_XML));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 2, mapping.size());
        assertTrue(BUG_WITH_GIVEN_HASHCODE_NOT_FOUND, mapping.containsKey(FIRST_WARNING));
        assertTrue(BUG_WITH_GIVEN_HASHCODE_NOT_FOUND, mapping.containsKey(SECOND_WARNING));

        assertEquals(WRONG_MESSAGE_PARSED, "Inconsistent synchronization of org.apache.hadoop.dfs.BlockCrcUpgradeObjectDatanode.blocksPreviouslyUpgraded; locked 85% of time", mapping.get(FIRST_WARNING));
        assertEquals(WRONG_MESSAGE_PARSED, "Should org.apache.hadoop.streaming.StreamJob$MultiPropertyOption be a _static_ inner class?", mapping.get(SECOND_WARNING));
    }

    /**
     * Checks whether we correctly detect a file in FindBugs native format.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void testFileWithMultipleLinesAndRanges() throws IOException, DocumentException, SAXException {
        scanNativeFile(FINDBUGS_NATIVE_XML, FINDBUGS_NATIVE_XML,
                Priority.NORMAL, "org/apache/hadoop/dfs/BlockCrcUpgrade.java", "org.apache.hadoop.dfs", 1309, 1309,
                5, "org/apache/hadoop/streaming/StreamJob.java", "org.apache.hadoop.streaming", 935, 980, 1);
    }

    /**
     * Checks whether, if a bug instance contains more than one element, we
     * correctly take the first one as referring to the buggy class.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void scanFileWarningsHaveMultipleClasses() throws IOException, DocumentException, SAXException {
        scanNativeFile("findbugs-multclass.xml", "FindBugs",
                Priority.HIGH, "umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 82, 82,
                1, "edu/umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 93, 93, 1);
    }

    /**
     * Checks whether we could also parse bugs of the fbcontrib plug-in.
     *
     * @throws IOException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void scanFbContribFile() throws IOException, DocumentException {
        MavenModule parseFile = parseFile("fbcontrib.xml");
        JavaPackage javaPackage = parseFile.getPackage("hudson.plugins.tasks");
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 16, javaPackage.getNumberOfAnnotations());

        boolean found = false;
        for (FileAnnotation annotation : javaPackage.getAnnotations()) {
            if (annotation.getFileName().contains("ResultSummary.java")) {
                found = true;
                assertFalse("Warning message could not be resolved.",
                        annotation.getToolTip().contains(
                                    "A warning was recorded, but findbugs can't find the description of this bug pattern"));
            }
        }
        assertTrue("No warning in class ResultSummary.java found.", found);
    }

    /**
     * Checks whether we generate a message if there is no message in the XML file.
     *
     * @throws IOException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void handleFilesWithoutMessages() throws IOException, DocumentException {
        MavenModule module = parseFile("findbugs-nomessage.xml");
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 1, module.getNumberOfAnnotations());

        FileAnnotation next = module.getAnnotations().iterator().next();
        assertEquals("Warning has no message.", "Redundant nullcheck of value known to be non-null", next.getMessage());
    }

    /**
     * Checks whether we correctly detect a file in FindBugs native format.
     *
     * @param findbugsFile
     *            name of the file to read
     * @param projectName
     *            name of the project
     * @param priority
     *            priority
     * @param fileName1
     *            first class filename
     * @param packageName1
     *            first class package name
     * @param start1
     *            start line of first class
     * @param end1
     *            end line of first class
     * @param ranges1
     *            number of line ranges for first class
     * @param fileName2
     *            second class filename
     * @param packageName2
     *            second class package name
     * @param start2
     *            start line of second class
     * @param end2
     *            end line of second class
     * @param ranges2
     *            number of line ranges for second class
     * @throws DocumentException
     *             on a parse error
     * @throws SAXException
     *             on a parse error
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public void scanNativeFile(final String findbugsFile, final String projectName,
            final Priority priority, final String fileName1, final String packageName1, final int start1, final int end1,
            final int ranges1, final String fileName2, final String packageName2, final int start2, final int end2, final int ranges2)
            throws IOException, DocumentException, SAXException {
   // CHECKSTYLE:ON
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile(findbugsFile);
        assertEquals("Wrong project name guessed", projectName, module.getName());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, NUMBER_OF_WARNINGS, module.getNumberOfAnnotations());
        Collection<FileAnnotation> warnings = module.getAnnotations();
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, NUMBER_OF_WARNINGS, warnings.size());


        Iterator<FileAnnotation> annotations = warnings.iterator();
        FileAnnotation annotation1 = annotations.next();
        FileAnnotation annotation2 = annotations.next();

        FileAnnotation firstAnnotation;
        FileAnnotation secondAnnotation;
        if (fileName1.equals(annotation1.getFileName())) {
            firstAnnotation = annotation1;
            secondAnnotation = annotation2;
        }
        else {
            firstAnnotation = annotation2;
            secondAnnotation = annotation1;
        }

        checkAnnotation(firstAnnotation, projectName, priority, fileName1, packageName1, start1, end1, ranges1);
        checkAnnotation(secondAnnotation, projectName, priority, fileName2, packageName2, start2, end2, ranges2);
    }

    /**
     * Checks an individual annotation.
     *
     * @param annotation
     *            the annotation to check
     * @param projectName
     *            name of the project
     * @param priority
     *            priority
     * @param fileName
     *            filename
     * @param packageName
     *            package name
     * @param start
     *            start line
     * @param end
     *            end line
     * @param ranges
     *            number of line ranges for first class
     */
    // CHECKSTYLE:OFF
    private void checkAnnotation(final FileAnnotation annotation, final String projectName, final Priority priority, final String fileName,
            final String packageName, final int start, final int end, final int ranges) {
    // CHECKSTYLE:ON
        assertEquals("Wrong file name parsed.", fileName, annotation.getFileName());
        assertEquals("Wrong package name parsed.", packageName, annotation.getPackageName());
        assertEquals("Wrong module name parsed.", projectName, annotation.getModuleName());

        assertFalse("Warning message could not be resolved.", annotation.getToolTip().contains("A warning was recorded, but findbugs can't find the description of this bug pattern"));

        Collection<LineRange> lineRanges = annotation.getLineRanges();
        assertEquals("Wrong number of line ranges parsed.", ranges, lineRanges.size());

        LineRange range = lineRanges.iterator().next();
        assertEquals("Wrong start of line range.", start, range.getStart());
        assertEquals("Wrong end of line range.", end, range.getEnd());

        assertEquals("Wrong priority parsed.", priority, annotation.getPriority());
        assertEquals("Wrong start of line range", start, annotation.getPrimaryLineNumber());

        assertFalse("No message for bug pattern detected", annotation.getToolTip().equals(Messages.FindBugs_Publisher_NoMessageFoundText()));
    }
}