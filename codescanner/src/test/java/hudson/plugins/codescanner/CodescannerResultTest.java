package hudson.plugins.codescanner;

import static junit.framework.Assert.*;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.test.BuildResultTest;

/**
 * Tests the class {@link CodescannerResult}.
 */
public class CodescannerResultTest extends BuildResultTest<CodescannerResult> {
    /** {@inheritDoc} */
    @Override
    protected CodescannerResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project) {
        return new CodescannerResult(build, null, project);
    }

    /** {@inheritDoc} */
    @Override
    protected CodescannerResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final CodescannerResult previous) {
        return new CodescannerResult(build, null, project, previous);
    }

    /** {@inheritDoc} */
    @Override
    protected void verifyHighScoreMessage(final int expectedZeroWarningsBuildNumber, final boolean expectedIsNewHighScore, final long expectedHighScore, final long gap, final CodescannerResult result) {
        if (result.hasNoAnnotations() && result.getDelta() == 0) {
            assertTrue(result.getDetails().contains(Messages.Codescanner_ResultAction_NoWarningsSince(expectedZeroWarningsBuildNumber)));
            if (expectedIsNewHighScore) {
                long days = BuildResult.getDays(expectedHighScore);
                if (days == 1) {
                    assertTrue(result.getDetails().contains(Messages.Codescanner_ResultAction_OneHighScore()));
                }
                else {
                    assertTrue(result.getDetails().contains(Messages.Codescanner_ResultAction_MultipleHighScore(days)));
                }
            }
            else {
                long days = BuildResult.getDays(gap);
                if (days == 1) {
                    assertTrue(result.getDetails().contains(Messages.Codescanner_ResultAction_OneNoHighScore()));
                }
                else {
                    assertTrue(result.getDetails().contains(Messages.Codescanner_ResultAction_MultipleNoHighScore(days)));
                }
            }
        }
    }
}

