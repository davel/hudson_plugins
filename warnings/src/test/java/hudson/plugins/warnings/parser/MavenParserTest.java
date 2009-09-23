package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link JavacParser} for output log of a maven compile.
 */
public class MavenParserTest extends ParserTester {
    /**
     * Creates a new instance of {@link MavenParserTest}.
     */
    public MavenParserTest() {
        super(JavacParser.class);
    }

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseMaven() throws IOException {
        Collection<FileAnnotation> warnings = sort(new JavacParser().parse(openFile()));

        assertEquals("Wrong number of warnings detected.", 5, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkMavenWarning(iterator.next(), 3);
        checkMavenWarning(iterator.next(), 36);
        checkMavenWarning(iterator.next(), 47);
        checkMavenWarning(iterator.next(), 69);
        checkMavenWarning(iterator.next(), 105);
    }

    /**
     * Verifies the annotation content.
     *
     * @param annotation the annotation to check
     * @param lineNumber
     *      the line number of the warning
     */
    private void checkMavenWarning(final FileAnnotation annotation, final int lineNumber) {
        checkWarning(annotation, lineNumber,
                "com.sun.org.apache.xerces.internal.impl.dv.util.Base64 is Sun proprietary API and may be removed in a future release",
                "/home/hudson/hudson/data/jobs/Hudson main/workspace/remoting/src/test/java/hudson/remoting/BinarySafeStreamTest.java",
                JavacParser.WARNING_TYPE, RegexpParser.PROPRIETARY_API, Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "maven.txt";
    }
}

