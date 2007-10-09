package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class FindBugsCounterTest {
    /** Error message. */
    private static final String WRONG_FIND_BUGS_FORMAT = "Wrong FindBugs format.";
    /** Error message. */
    private static final String WRONG_VERSION_DETECTED = "Wrong Version detected";
    /** Error message. */
    private static final String NO_FILE_NAME_FOUND = "No file name found.";
    /** Package of documentation warnings. */
    private static final String DOCU_PACKAGE = "com.avaloq.adt.internal.ui.docu";
    /** Package of spell checker warnings. */
    private static final String SPELL_PACKAGE = "com.avaloq.adt.internal.ui.spell";
    /** Expected number of documentation warnings. */
    private static final int NUMBER_OF_DOCU_WARNINGS = 2;
    /** Expected number of spell checker warnings. */
    private static final int NUMBER_OF_SPELL_WARNINGS = 3;
    /** Error message. */
    private static final String WRONG_WARNINGS_IN_PACKAGE_ERROR = "Wrong number of warnings in a package detected.";
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";

    /**
     * Initializes the messages file.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     */
    @BeforeClass
    public static void initialize() throws IOException, SAXException {
        FindBugsMessages.getInstance().initialize();
    }

    /**
     * Checks whether we correctly detect that the file contains no bugs.
     */
    @Test
    public void scanFileWithNoBugs() throws IOException, SAXException {
        Module module = parseFile("findbugs-no-errors.xml");
        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfWarnings());
    }

    /**
     * Parses the specified file.
     *
     * @param fileName the file to read
     * @return the parsed module
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     */
    private Module parseFile(final String fileName) throws IOException, SAXException {
        URL file = FindBugsCounterTest.class.getResource(fileName);
        return new FindBugsCounter(null).parse(file);
    }

    /**
     * Checks whether we correctly detect an other file.
     */
    @Test
    public void scanOtherFile() throws IOException, SAXException {
        Module module = parseFile("otherfile.xml");
        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfWarnings());
        assertEquals(ERROR_MESSAGE, "Unknown file format", module.getName());
    }

    /**
     * Checks whether we correctly detect a file in FindBugs native format.
     */
    @Test
    public void scanNativeFile() throws IOException, SAXException {
        Module module = parseFile("findbugs-native.xml");
        assertEquals(ERROR_MESSAGE, 128, module.getNumberOfWarnings());
        assertEquals(WRONG_VERSION_DETECTED, "1.2.1", module.getVersion());
        assertFalse(WRONG_FIND_BUGS_FORMAT, module.isMavenFormat());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 0, module.getNumberOfWarnings("java.lang"));

        for (Warning warning : module.getWarnings("org.apache.hadoop.ipc")) {
            assertNotNull("Message should not be empty.", warning.getMessage());
            assertNotNull("Line number should not be empty.", warning.getLineNumber());

            assertNotNull(NO_FILE_NAME_FOUND, warning.getFile());
        }
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanFileWithSomeBugs() throws IOException, SAXException {
        Module module = parseFile("findbugs.xml");
        assertEquals(ERROR_MESSAGE, NUMBER_OF_SPELL_WARNINGS + NUMBER_OF_DOCU_WARNINGS, module.getNumberOfWarnings());
        assertEquals(WRONG_VERSION_DETECTED, "1.2.0", module.getVersion());
        assertEquals("Wrong number of packages detected", 2, module.getPackages().size());
        assertTrue(WRONG_FIND_BUGS_FORMAT, module.isMavenFormat());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_SPELL_WARNINGS, module.getWarnings(SPELL_PACKAGE).size());
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_DOCU_WARNINGS, module.getWarnings(DOCU_PACKAGE).size());
        JavaProject javaProject = new JavaProject();
        javaProject.addModule(module);
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_SPELL_WARNINGS, javaProject.getWarnings(SPELL_PACKAGE).size());
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_DOCU_WARNINGS, javaProject.getWarnings(DOCU_PACKAGE).size());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 0, javaProject.getWarnings("wrong.package").size());

        Collection<Warning> warnings = javaProject.getWarnings(SPELL_PACKAGE);
        for (Warning warning : warnings) {
            assertEquals("Wrong class found.", "SpellingContentAssistProcessor", warning.getClassname());
        }
    }

    /**
     * Checks whether, if a bug instance contains more than one
     * element, we correctly take the first one as referring to the
     * buggy class.
     */
    @Test
    public void scanFileWarningsHaveMultipleClasses() throws IOException, SAXException {
        Module module = parseFile("findbugs-multclass.xml");
        assertEquals(WRONG_VERSION_DETECTED, "1.2.1", module.getVersion());
        assertEquals(ERROR_MESSAGE, 2, module.getNumberOfWarnings());
        assertFalse(WRONG_FIND_BUGS_FORMAT, module.isMavenFormat());
        Collection<Warning> warnings = module.getWarnings();
        for (Warning warning : warnings) {
            assertTrue("Wrong package prefix found.", warning.getPackageName().startsWith("edu.umd"));
            assertNotNull(NO_FILE_NAME_FOUND, warning.getFile());
        }

        assertEquals("Wrong number of source paths detected", 2, module.getProjectInformation().getSourcePaths().size());
    }

    /**
     * Checks whether we correctly assign source paths when the source directory
     * folder is specified in the FindBugs native file format. element, we
     * correctly take the first one as referring to the buggy class.
     */
    @Test
    public void checkSourcePathComposition() throws IOException, SAXException {
        Module module = parseFile("srcpath.xml");
        assertEquals(WRONG_VERSION_DETECTED, "1.2.1", module.getVersion());
        assertEquals(ERROR_MESSAGE, 1, module.getNumberOfWarnings());
        assertFalse(WRONG_FIND_BUGS_FORMAT, module.isMavenFormat());
        Warning warning = module.getWarnings().iterator().next();

        assertEquals("Wrong filename guessed.", "!usr!local!tomcat!hudson!jobs!FindBugs%20Test!workspace!findBugsTest!src!org!example!SyncBug.java", warning.getFile());
    }
}


/* Copyright (c) Avaloq Evolution AG */