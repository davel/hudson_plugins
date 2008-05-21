package hudson.plugins.pmd;

import hudson.model.AbstractBuild;
import hudson.plugins.pmd.util.model.JavaProject;

/**
 * Creates a new PMD result based on the values of a previous build and the
 * current project.
 *
 * @author Ulli Hafner
 */
public class PmdResultBuilder {
    /**
     * Creates a result that persists the PMD information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @return the result action
     */
    public PmdResult build(final AbstractBuild<?, ?> build, final JavaProject project) {
        Object previous = build.getPreviousBuild();
        while (previous instanceof AbstractBuild<?, ?> && previous != null) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            PmdResultAction previousAction = previousBuild.getAction(PmdResultAction.class);
            if (previousAction != null) {
                return new PmdResult(build, project, previousAction.getResult().getProject(),
                        previousAction.getResult().getZeroWarningsHighScore());
            }
            previous = previousBuild.getPreviousBuild();
        }
        return new PmdResult(build, project);
    }
}

