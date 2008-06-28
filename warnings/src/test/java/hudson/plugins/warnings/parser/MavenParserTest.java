package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link JavacParser}.
 */
public class MavenParserTest extends ParserTester {
    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseMaven() throws IOException {
        Collection<FileAnnotation> warnings = new MavenParser().parse(MavenParserTest.class.getResourceAsStream("maven.txt"));

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
                MavenParser.WARNING_TYPE, RegexpParser.PROPRIETARY_API, Priority.NORMAL);
    }
}

