package hudson.plugins.analysis.collector;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.BuildResultGraph;

import java.util.List;

/**
 * Entry point to visualize the trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link AnalysisResultAction}.
 *
 * @author Ulli Hafner
 */
public class AnalysisProjectAction extends AbstractProjectAction<AnalysisResultAction> {
    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public AnalysisProjectAction(final AbstractProject<?, ?> project) {
        super(project, AnalysisResultAction.class, new AnalysisDescriptor());
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Analysis_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.Analysis_Trend_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected void registerAvailableGraphs(final List<BuildResultGraph> availableGraphs) {
        availableGraphs.add(new OriginGraph());
    }
}

