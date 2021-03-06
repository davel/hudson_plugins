package hudson.plugins.cpptest;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.cpptest.parser.CpptestParser;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the Cpptest analysis  (freestyle project type).
 *
 * @author Ulli Hafner
 * 
 * NQH: adapt for Cpptest
 */
public class CpptestPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 6369581633551160418L;

    /** Default Cpptest pattern. */
    private static final String DEFAULT_PATTERN = "**/Cpptest-result.xml";
    /** Ant file-set pattern of files to work with. */
    private final String pattern;

    /**
     * Creates a new instance of <code>CpptestPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern to scan for Cpptest files
     * @param threshold
     *            Annotation threshold to be reached if a build should be considered as
     *            unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be considered as
     *            failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     */
    // Cpptest:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public CpptestPublisher(final String pattern, final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding, final boolean useDeltaValues) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues, "Cpptest");
        this.pattern = pattern;
    }
    // Cpptest:ON

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new CpptestProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Collecting Cpptest analysis files...");

        FilesParser parser = new FilesParser(logger, StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                new CpptestParser(getDefaultEncoding()),
                isMavenBuild(build), isAntBuild(build));
        ParserResult project = build.getWorkspace().act(parser);
        CpptestResult result = new CpptestResult(build, getDefaultEncoding(), project);
        build.getActions().add(new CpptestResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public CpptestDescriptor getDescriptor() {
        return (CpptestDescriptor)super.getDescriptor();
    }
}
