package hudson.plugins.tasks.parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Detects the package name of a Java file.
 *
 * @author Ulli Hafner
 */
public class JavaPackageDetector implements PackageDetector {
    /** {@inheritDoc}*/
    public String detectPackageName(final InputStream stream) throws IOException {
        try {
            LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if (line.matches("^package .*;$")) {
                    return StringUtils.substringBetween(line, " ", ";").trim();
                }
            }
            return StringUtils.EMPTY;
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /** {@inheritDoc} */
    public boolean accepts(final String fileName) {
        return fileName.endsWith(".java");
    }
}

