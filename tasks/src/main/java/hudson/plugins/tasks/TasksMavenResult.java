package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.tasks.parser.TasksParserResult;


/**
 * Represents the aggregated results of the PMD analysis in m2 jobs.
 *
 * @author Ulli Hafner
 */
public class TasksMavenResult extends TasksResult {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -4913938782537266259L;

    /**
     * Creates a new instance of {@link TasksMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed annotations
     * @param highTags
     *            tag identifiers indicating high priority
     * @param normalTags
     *            tag identifiers indicating normal priority
     * @param lowTags
     *            tag identifiers indicating low priority
     */
    public TasksMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final TasksParserResult result,
            final String highTags, final String normalTags, final String lowTags) {
        super(build, defaultEncoding, result, highTags, normalTags, lowTags);
    }

    /**
     * Creates a new instance of {@link TasksMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed FindBugs result
     * @param previous
     *            the previous result of open tasks
     * @param highTags
     *            tag identifiers indicating high priority
     * @param normalTags
     *            tag identifiers indicating normal priority
     * @param lowTags
     *            tag identifiers indicating low priority
     */
    public TasksMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final TasksParserResult result, final BuildResult previous,
            final String highTags, final String normalTags, final String lowTags) {
        super(build, defaultEncoding, result, previous, highTags, normalTags, lowTags);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return MavenTasksResultAction.class;
    }
}

