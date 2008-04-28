package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.model.JavaProject;

/**
 * Creates a new FindBugs result based on the values of a previous build and the
 * current project.
 */
public class FindBugsResultBuilder {
    /**
     * Creates a result that persists the FindBugs information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @return the result action
     */
    public FindBugsResult build(final AbstractBuild<?, ?> build, final JavaProject project) {
        Object previous = build.getPreviousBuild();
        if (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            FindBugsResultAction previousAction = previousBuild.getAction(FindBugsResultAction.class);
            if (previousAction != null) {
                return new FindBugsResult(build, project,
                        previousAction.getResult().getProject(),
                        previousAction.getResult().getZeroWarningsHighScore());
            }
        }
        return new FindBugsResult(build, project);
    }
}

