package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.MavenModule;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.views.ModuleDetail;

import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Represents the details of a maven module.
 *
 * @author Ulli Hafner
 */
public class TasksModuleDetail extends ModuleDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3743047168363581305L;
    /** Handles the task tags. */
    private final TaskTagsHandler taskTagsHandler;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            the current build as owner of this result object
     * @param module
     *            the selected module to show
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksModuleDetail(final AbstractBuild<?, ?> owner, final MavenModule module, final String defaultEncoding, final String header,
            final String high, final String normal, final String low) {
        super(owner, module, defaultEncoding, header);

        taskTagsHandler = new TaskTagsHandler(high, normal, low, module);
    }

    /** {@inheritDoc} */
    @Override
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new TasksDetailBuilder().getDynamic(link, getOwner(), getContainer(), getDefaultEncoding(), getDisplayName(),
                    getTags(Priority.HIGH), getTags(Priority.NORMAL), getTags(Priority.LOW));
    }

    // CHECKSTYLE:OFF - generated delegate -

    /**
     * Returns all priorities that have a user defined tag.
     *
     * @return all priorities that have a user defined tag
     */
    public Collection<String> getAvailablePriorities() {
        return taskTagsHandler.getAvailablePriorities();
    }

    /** {@inheritDoc} */
    @Override
    public Priority[] getPriorities() {
        return taskTagsHandler.getPriorities();
    }

    /**
     * Returns the defined tags for the given priority.
     *
     * @param priority the priority
     * @return the defined tags for the given priority
     */
    public final String getTags(final Priority priority) {
        return taskTagsHandler.getTags(priority);
    }

    /**
     * Returns the defined tags for the given priority.
     *
     * @param priority the priority
     * @return the defined tags for the given priority
     */
    public final String getTags(final String priority) {
        return taskTagsHandler.getTags(priority);
    }
}
