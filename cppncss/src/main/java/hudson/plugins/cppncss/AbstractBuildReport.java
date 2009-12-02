package hudson.plugins.cppncss;

import hudson.model.AbstractBuild;
import hudson.model.HealthReportingAction;
import hudson.plugins.cppncss.parser.StatisticsResult;
import hudson.plugins.cppncss.parser.StatisticsTotalResult;
import hudson.plugins.helpers.AbstractBuildAction;
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
 * @since 09-Jan-2008 21:19:37
 */
public abstract class AbstractBuildReport<T extends AbstractBuild<?, ?>> extends AbstractBuildAction<T> implements HealthReportingAction {
    private final StatisticsResult results;
    private final StatisticsTotalResult totals;
	private final Integer functionCcnViolationThreshold;
	private final Integer functionNcssViolationThreshold;

    /**
     * Constructs a new AbstractBuildReport.
     */
    public AbstractBuildReport(StatisticsResult results, Integer functionCcnViolationThreshold, Integer functionNcssViolationThreshold) {
        this.results = results;
		this.functionCcnViolationThreshold = functionCcnViolationThreshold;
		this.functionNcssViolationThreshold = functionNcssViolationThreshold;
        this.totals = StatisticsResult.total(results);
    }

    public StatisticsResult getResults() {
        return results;
    }

    public StatisticsTotalResult getTotals() {
        return totals;
    }

    public Integer getFunctionCcnViolationThreshold() {
		return functionCcnViolationThreshold;
	}
    
    public Integer getFunctionNcssViolationThreshold(){
    	return functionNcssViolationThreshold;
    }

	/**
     * The summary of this build report for display on the build index page.
     *
     * @return
     */
    public String getSummary() {
        AbstractBuild<?, ?> prevBuild = getBuild().getPreviousBuild();
        while (prevBuild != null && prevBuild.getAction(getClass()) == null) {
            prevBuild = prevBuild.getPreviousBuild();
        }
        if (prevBuild == null) {
            return totals.toSummary();
        } else {
            AbstractBuildReport action = prevBuild.getAction(getClass());
            return totals.toSummary(action.getTotals());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        return PluginImpl.ICON_FILE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return PluginImpl.DISPLAY_NAME;
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

        Calendar t = getBuild().getTimestamp();

        if (req.checkIfModified(t, rsp)) {
            return; // up to date
        }

        ChartUtil.generateGraph(req, rsp, GraphHelper.buildChart(getBuild(), functionCcnViolationThreshold, functionNcssViolationThreshold), getGraphWidth(), getGraphHeight());
    }


    /**
     * Returns <code>true</code> if there is a graph to plot.
     *
     * @return Value for property 'graphAvailable'.
     */
    public boolean isGraphActive() {
        AbstractBuild<?, ?> build = getBuild();
        // in order to have a graph, we must have at least two points.
        int numPoints = 0;
        while (numPoints < 2) {
            if (build == null) {
                return false;
            }
            if (build.getAction(getClass()) != null) {
                numPoints++;
            }
            build = build.getPreviousBuild();
        }
        return true;
    }

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
