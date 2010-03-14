package hudson.plugins.codescanner;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.test.BuildResultTest;

/**
 * Tests the class {@link CodescannerResult}.
 */
public class CodescannerResultTest extends BuildResultTest<CodescannerResult> {
    /** {@inheritDoc} */
    @Override
    protected CodescannerResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final BuildHistory history) {
        return new CodescannerResult(build, null, project, history);
    }
}

