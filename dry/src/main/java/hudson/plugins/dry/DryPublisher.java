package hudson.plugins.dry;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.plugins.dry.parser.DuplicationParserRegistry;
import hudson.plugins.dry.util.BuildResult;
import hudson.plugins.dry.util.FilesParser;
import hudson.plugins.dry.util.HealthAwarePublisher;
import hudson.plugins.dry.util.ParserResult;
import hudson.plugins.dry.util.PluginLogger;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the duplicate code analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class DryPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 6711252664481150129L;
    /** Default DRY pattern. */
    private static final String DEFAULT_PATTERN = "**/cpd.xml";
    /** Descriptor of this publisher. */
    public static final DryDescriptor DRY_DESCRIPTOR = new DryDescriptor();
    /** Ant file-set pattern of files to work with. */
    private final String pattern;

    /**
     * Creates a new instance of <code>PmdPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern to scan for DRY files
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
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public DryPublisher(final String pattern, final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, defaultEncoding, "DRY");
        this.pattern = pattern;
    }
    // CHECKSTYLE:ON

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
        return new DryProjectAction(project, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Collecting duplicate code analysis files...");
        FilesParser dryCollector = new FilesParser(logger, StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN), new DuplicationParserRegistry(),
                isMavenBuild(build), isAntBuild(build));

        ParserResult project = build.getProject().getWorkspace().act(dryCollector);
        DryResult result = new DryResultBuilder().build(build, project, getDefaultEncoding());
        build.getActions().add(new DryResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor<Publisher> getDescriptor() {
        return DRY_DESCRIPTOR;
    }
}
