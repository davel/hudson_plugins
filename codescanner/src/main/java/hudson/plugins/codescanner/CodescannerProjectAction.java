package hudson.plugins.codescanner;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Entry point to visualize the warnings trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link CodescannerResultAction}.
 *
 * @author Maximilian Odendahl
 */
public class CodescannerProjectAction extends AbstractProjectAction<CodescannerResultAction> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -654316141132780562L;

    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public CodescannerProjectAction(final AbstractProject<?, ?> project) {
        super(project, CodescannerResultAction.class, CodescannerPublisher.CODESCANNER_DESCRIPTOR);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Codescanner_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.Codescanner_Trend_Name();
    }
}

