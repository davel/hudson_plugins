package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.plugins.tasks.parser.TasksProject;
import hudson.plugins.tasks.parser.WorkspaceScanner;
import hudson.plugins.tasks.util.HealthAwarePublisher;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Publishes the results of the task scanner.
 *
 * @author Ulli Hafner
 */
public class TasksPublisher extends HealthAwarePublisher {
    /** Default files pattern. */
    private static final String DEFAULT_PATTERN = "**/*.java";
    /** Descriptor of this publisher. */
    public static final TasksDescriptor TASK_SCANNER_DESCRIPTOR = new TasksDescriptor();
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;

    /**
     * Creates a new instance of <code>TaskScannerPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern of files to scan for open tasks in
     * @param threshold
     *            Tasks threshold to be reached if a build should be considered
     *            as unstable.
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @stapler-constructor
     */
    public TasksPublisher(final String pattern, final String threshold,
            final String healthy, final String unHealthy,
            final String high, final String normal, final String low) {
        super(pattern, threshold, healthy, unHealthy);

        this.high = high;
        this.normal = normal;
        this.low = low;
    }

    /**
     * Returns the high priority task identifiers.
     *
     * @return the high priority task identifiers
     */
    public String getHigh() {
        return high;
    }

    /**
     * Returns the normal priority task identifiers.
     *
     * @return the normal priority task identifiers
     */
    public String getNormal() {
        return normal;
    }

    /**
     * Returns the low priority task identifiers.
     *
     * @return the low priority task identifiers
     */
    public String getLow() {
        return low;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new TasksProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public TasksProject perform(final AbstractBuild<?, ?> build, final BuildListener listener) throws InterruptedException, IOException {
        TasksProject project;
        listener.getLogger().println("Scanning workspace files for tasks...");
        project = build.getProject().getWorkspace().act(
                new WorkspaceScanner(StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN), high, normal, low));

        Object previous = build.getPreviousBuild();
        TasksResult result;
        if (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            TasksResultAction previousAction = previousBuild.getAction(TasksResultAction.class);
            if (previousAction == null) {
                result = new TasksResult(build, project, high, normal, low);
            }
            else {
                result = new TasksResult(build, project, previousAction.getResult().getNumberOfAnnotations(), high, normal, low);
            }
        }
        else {
            result = new TasksResult(build, project, high, normal, low);
        }

        HealthReportBuilder healthReportBuilder = createHealthReporter(
                Messages.Tasks_ResultAction_HealthReportSingleItem(),
                Messages.Tasks_ResultAction_HealthReportMultipleItem("%d"));
        build.getActions().add(new TasksResultAction(build, result, healthReportBuilder));

        return project;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return TASK_SCANNER_DESCRIPTOR;
    }
}
