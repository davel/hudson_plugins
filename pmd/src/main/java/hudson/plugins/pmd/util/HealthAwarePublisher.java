package hudson.plugins.pmd.util;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.pmd.util.model.JavaProject;
import hudson.tasks.BuildStep;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;

/**
 * A base class for publishers with the following two characteristics:
 * <ul>
 * <li>It provides a unstable threshold, that could be enabled and set in the
 * configuration screen. If the number of annotations in a build exceeds this
 * value then the build is considered as {@link Result#UNSTABLE UNSTABLE}.
 * </li>
 * <li>It provides thresholds for the build health, that could be adjusted in
 * the configuration screen. These values are used by the
 * {@link HealthReportBuilder} to compute the health and the health trend graph.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
public abstract class HealthAwarePublisher extends Publisher {
    /** Annotation threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    private boolean thresholdEnabled;
    /** Integer threshold to be reached if a build should be considered as unstable. */
    private int minimumAnnotations;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthyAnnotations;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthyAnnotations;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean healthyReportEnabled;
    /** Determines the height of the trend graph. */
    private final String height;
    /** The name of the plug-in. */
    private final String pluginName;

    /**
     * Creates a new instance of <code>HealthAwarePublisher</code>.
     *
     * @param threshold
     *            Tasks threshold to be reached if a build should be considered
     *            as unstable.
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @param pluginName
     *            the name of the plug-in
     */
    public HealthAwarePublisher(final String threshold, final String healthy, final String unHealthy,
            final String height, final String pluginName) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.height = height;
        this.pluginName = "[" + pluginName + "] ";

        if (!StringUtils.isEmpty(threshold)) {
            try {
                minimumAnnotations = Integer.valueOf(threshold);
                if (minimumAnnotations >= 0) {
                    thresholdEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        if (!StringUtils.isEmpty(healthy) && !StringUtils.isEmpty(unHealthy)) {
            try {
                healthyAnnotations = Integer.valueOf(healthy);
                unHealthyAnnotations = Integer.valueOf(unHealthy);
                if (healthyAnnotations >= 0 && unHealthyAnnotations > healthyAnnotations) {
                    healthyReportEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        if (canContinue(build.getResult())) {
            PrintStream logger = listener.getLogger();
            try {
                JavaProject project = perform(build, logger);
                evaluateBuildResult(build, logger, project);
            }
            catch (AbortException exception) {
                logger.println(exception.getMessage());
                build.setResult(Result.FAILURE);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the publisher can continue processing. This default
     * implementation returns <code>true</code> if the build is not aborted or failed.
     *
     * @param result build result
     * @return <code>true</code> if the build can continue
     */
    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED && result != Result.FAILURE;
    }

    /**
     * Performs the publishing of the results of this plug-in.
     *
     * @param build
     *            the build
     * @param logger the logger to report the progress to
     *
     * @return the java project containing the found annotations
     *
     * @throws InterruptedException
     *             If the build is interrupted by the user (in an attempt to
     *             abort the build.) Normally the {@link BuildStep}
     *             implementations may simply forward the exception it got from
     *             its lower-level functions.
     * @throws IOException
     *             If the implementation wants to abort the processing when an
     *             {@link IOException} happens, it can simply propagate the
     *             exception to the caller. This will cause the build to fail,
     *             with the default error message. Implementations are
     *             encouraged to catch {@link IOException} on its own to provide
     *             a better error message, if it can do so, so that users have
     *             better understanding on why it failed.
     */
    protected abstract JavaProject perform(AbstractBuild<?, ?> build, PrintStream logger) throws InterruptedException, IOException;

    /**
     * Evaluates the build result. The build is marked as unstable if the
     * threshold has been exceeded.
     *
     * @param build
     *            the build to create the action for
     * @param logger
     *            the logger
     * @param project
     *            the project with the annotations
     */
    private void evaluateBuildResult(final AbstractBuild<?, ?> build, final PrintStream logger, final JavaProject project) {
        int annotationCount = project.getNumberOfAnnotations();
        if (annotationCount > 0) {
            log(logger, "A total of " + annotationCount + " annotations have been found.");
            if (isThresholdEnabled() && annotationCount >= getMinimumAnnotations()) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            log(logger, "No annotations have been found.");
        }
    }

    /**
     * Logs the specified message.
     *
     * @param logger the logger
     * @param message the message
     */
    protected void log(final PrintStream logger, final String message) {
        logger.println(StringUtils.defaultString(pluginName) + message);
    }

    /**
     * Creates a new instance of <code>HealthReportBuilder</code>.
     *
     * @param reportSingleCount
     *            message to be shown if there is exactly one item found
     * @param reportMultipleCount
     *            message to be shown if there are zero or more than one items
     *            found
     * @return the new health report builder
     */
    protected HealthReportBuilder createHealthReporter(final String reportSingleCount, final String reportMultipleCount) {
        return new HealthReportBuilder(thresholdEnabled, minimumAnnotations, healthyReportEnabled, healthyAnnotations, unHealthyAnnotations,
                reportSingleCount, reportMultipleCount);
    }


    /**
     * Determines whether a threshold has been defined.
     *
     * @return <code>true</code> if a threshold has been defined
     */
    public boolean isThresholdEnabled() {
        return thresholdEnabled;
    }

    /**
     * Returns the annotation threshold to be reached if a build should be considered as unstable.
     *
     * @return the annotation threshold to be reached if a build should be considered as unstable.
     */
    public String getThreshold() {
        return threshold;
    }

    /**
     * Returns the threshold to be reached if a build should be considered as unstable.
     *
     * @return the threshold to be reached if a build should be considered as unstable
     */
    public int getMinimumAnnotations() {
        return minimumAnnotations;
    }

    /**
     * Returns the isHealthyReportEnabled.
     *
     * @return the isHealthyReportEnabled
     */
    public boolean isHealthyReportEnabled() {
        return healthyReportEnabled;
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    public String getHealthy() {
        return healthy;
    }

    /**
     * Returns the healthy threshold for annotations, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    public int getHealthyAnnotations() {
        return healthyAnnotations;
    }

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    public String getUnHealthy() {
        return unHealthy;
    }

    /**
     * Returns the unhealthy threshold of annotations, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    public int getUnHealthyAnnotations() {
        return unHealthyAnnotations;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public String getHeight() {
        return height;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public int getTrendHeight() {
        return new TrendReportSize(height).getHeight();
    }
}
