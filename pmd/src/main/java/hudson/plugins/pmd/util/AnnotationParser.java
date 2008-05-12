package hudson.plugins.pmd.util;

import hudson.plugins.pmd.util.model.MavenModule;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * A parser for annotations.
 *
 * @author Ulli Hafner
 */
public interface AnnotationParser extends Serializable {
    /**
     * Returns the annotations found in the specified file.
     *
     * @param file
     *            the file to parse
     * @param moduleName
     *            name of the maven module
     * @return the parsed result (stored in the module instance)
     * @throws InvocationTargetException
     *             if the file could not be parsed (wrap your exception in this exception)
     */
    MavenModule parse(final InputStream file, final String moduleName) throws InvocationTargetException;

    /**
     * Returns the name of this parser.
     *
     * @return the name of this parser
     */
    String getName();
}

