package hudson.plugins.codescanner;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.ParserResult;


/**
 * Creates a new warnings result based on the values of a previous build and the
 * current project.
 *
 * @author Maximilian Odendahl
 */
public class CodescannerResultBuilder {
    /**
     * Creates a result that persists the warnings information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param result
     *            the result containing the annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @return the result action
     */
    public CodescannerResult build(final AbstractBuild<?, ?> build, final ParserResult result, final String defaultEncoding) {
        Object previous = build.getPreviousBuild();
        while (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            CodescannerResultAction previousAction = previousBuild.getAction(CodescannerResultAction.class);
            if (previousAction != null) {
                return new CodescannerResult(build, defaultEncoding, result, previousAction.getResult());
            }
            previous = previousBuild.getPreviousBuild();
        }
        return new CodescannerResult(build, defaultEncoding, result);
    }
}

