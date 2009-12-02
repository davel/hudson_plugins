package hudson.plugins.cppncss;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.plugins.cppncss.parser.StatisticsResult;
import hudson.plugins.cppncss.parser.StatisticsTotalResult;
import hudson.plugins.helpers.AbstractProjectAction;
import hudson.plugins.helpers.GraphHelper;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Calendar;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 09-Jan-2008 21:22:45
 */
public abstract class AbstractProjectReport<T extends AbstractProject<?, ?>> extends AbstractProjectAction<T>
        implements ProminentProjectAction {

    public AbstractProjectReport(T project, Integer functionCcnViolationThreshold, Integer functionNcssViolationThreshold) {
        super(project, functionCcnViolationThreshold, functionNcssViolationThreshold);
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
            final AbstractBuildReport action = build.getAction(getBuildActionClass());
            if (action != null) {
                return PluginImpl.ICON_FILE_NAME;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
            final AbstractBuildReport action = build.getAction(getBuildActionClass());
            if (action != null) {
                return PluginImpl.DISPLAY_NAME;
            }
        }
        return null;
    }

    /**
     * Getter for property 'graphName'.
     *
     * @return Value for property 'graphName'.
     */
    public String getGraphName() {
        return PluginImpl.GRAPH_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName() {
        for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
            final AbstractBuildReport action = build.getAction(getBuildActionClass());
            if (action != null) {
                return PluginImpl.URL;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getSearchUrl() {
        return PluginImpl.URL;
    }

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (GraphHelper.isGraphUnsupported()) {
            GraphHelper.redirectWhenGraphUnsupported(rsp, req);
            return;
        }

        Calendar t = getProject().getLastBuild().getTimestamp();

        if (req.checkIfModified(t, rsp)) {
            return; // up to date
        }

        ChartUtil.generateGraph(req, rsp, GraphHelper.buildChart(getProject().getLastBuild(), getFunctionCcnViolationThreshold(), getFunctionNcssViolationThreshold()), getGraphWidth(),
                getGraphHeight());
    }

    /**
     * Returns <code>true</code> if there is a graph to plot.
     *
     * @return Value for property 'graphAvailable'.
     */
    public boolean isGraphActive() {
        AbstractBuild<?, ?> build = getProject().getLastBuild();
        // in order to have a graph, we must have at least two points.
        int numPoints = 0;
        while (numPoints < 2) {
            if (build == null) {
                return false;
            }
            if (build.getAction(getBuildActionClass()) != null) {
                numPoints++;
            }
            build = build.getPreviousBuild();
        }
        return true;
    }

    /**
     * Returns the latest results.
     *
     * @return Value for property 'graphAvailable'.
     */
    public StatisticsResult getResults() {
        for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
            final AbstractBuildReport action = build.getAction(getBuildActionClass());
            if (action != null) {
                return action.getResults();
            }
        }
        return new StatisticsResult();
    }

    /**
     * Returns the latest totals.
     *
     * @return Value for property 'graphAvailable'.
     */
    public StatisticsTotalResult getTotals() {
        for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild()) {
            final AbstractBuildReport action = build.getAction(getBuildActionClass());
            if (action != null) {
                return action.getTotals();
            }
        }
        return null;
    }

    protected abstract Class<? extends AbstractBuildReport> getBuildActionClass();

    /**
     * Getter for property 'graphWidth'.
     *
     * @return Value for property 'graphWidth'.
     */
    public int getGraphWidth() {
        return 500;
    }

    /**
     * Getter for property 'graphHeight'.
     *
     * @return Value for property 'graphHeight'.
     */
    public int getGraphHeight() {
        return 200;
    }

}
