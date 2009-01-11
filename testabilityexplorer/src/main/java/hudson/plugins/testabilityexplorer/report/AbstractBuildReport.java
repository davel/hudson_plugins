package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.charts.BuildAndResults;
import hudson.plugins.testabilityexplorer.report.charts.RangedOverallTrend;
import hudson.plugins.testabilityexplorer.report.charts.RangedTrend;
import hudson.plugins.testabilityexplorer.report.charts.RangedClassesTrend;
import hudson.plugins.testabilityexplorer.helpers.AbstractBuildAction;
import hudson.plugins.testabilityexplorer.PluginImpl;

import java.util.*;
import java.io.IOException;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

/**
 * Abstract {@link AbstractBuildAction} class that is capable of rendering different build reports.
 *
 * @author reik.schatz
 */
public abstract class AbstractBuildReport<T extends AbstractBuild<?, ?>> extends AbstractBuildAction<T>
{
    private final Collection<Statistic> m_results;
    private final ReportBuilder m_reportBuilder;
    private final CostDetailBuilder m_detailBuilder;

    public AbstractBuildReport(Collection<Statistic> results, ReportBuilder reportBuilder, CostDetailBuilder detailBuilder) {
        m_results = results;
        m_reportBuilder = reportBuilder;
        m_detailBuilder = detailBuilder;
    }

    public Collection<Statistic> getResults()
    {
        return m_results == null ? new ArrayList<Statistic>() : new ArrayList<Statistic>(m_results);
    }

    void addResults(Collection<Statistic> statistics)
    {
        for (Statistic statistic : statistics)
        {
            if (!m_results.contains(statistic))
            {
                m_results.add(statistic);
            }
        }
    }

    /** {@inheritDoc} */
    public String getSummary()
    {
        String summary = "";

        AbstractBuild<?, ?> build = getBuild();
        if (build != null)
        {
            summary = " (Total: " + getTotals() + ")";
        }
        return summary;
    }

    public int getTotals()
    {
        int total = 0;
        for (Statistic statistic : getResults())
        {
            CostSummary summary = statistic.getCostSummary();
            if (summary != null)
            {
                total += summary.getTotal();
            }
        }
        return total;
    }

    /**
     * This will be called implicitly by Stapler framework, if there is a dynamic part in the regular link.<br />
     * Example: lets say your plugin is called <code>testability</code>. When requesting <code>http://hudson:8080/job/project/build/testability/foo</code>
     * this method gets called with <code>foo</code> as first parameter.
     *
     * @param link the dynamic part of the url after the plugin name
     * @param request StaplerRequest
     * @param response StaplerResponse
     * @return any object that you want to work with in .jelly files
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response)
    {
        String originalRequestUri = request.getOriginalRequestURI();
        return m_detailBuilder.buildDetail(link, originalRequestUri, getBuild(), getResults());
    }

    /** {@inheritDoc} */
    public HealthReport getBuildHealth()
    {
        return m_reportBuilder.computeHealth(getResults());
    }

    /**
     * Renders a jfree chart graph into the given {@link StaplerResponse}. If the given {@link StaplerRequest} contains a
     * paramter {@code classes} set to {@code true}, a classes trend graph instead of the overall trend graph will be rendered.
     *
     * @param request a StaplerRequest
     * @param response a StaplerResponse
     * @param height the height of the charts (not used for now)
     * @throws IOException if there is a problem writing to the StaplerResponse
     */
    public final void doTrendGraph(final StaplerRequest request, final StaplerResponse response, final int height) throws IOException
    {
        if (ChartUtil.awtProblem)
        {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }

        String classesTrend = request.getParameter("classes");
        boolean displayClassesTrend = Boolean.valueOf(StringUtils.defaultIfEmpty(classesTrend, "false"));

        List<BuildAndResults> buildsAndResults = retrieveExistingBuildsAndResults(getBuild());
        if (displayClassesTrend)
        {
            RangedTrend rangedTrend = new RangedClassesTrend(buildsAndResults);
            JFreeChart chart = createChart(rangedTrend);
            ChartUtil.generateGraph(request, response, chart, 800, 600);
        }
        else
        {
            RangedTrend rangedTrend = new RangedOverallTrend(buildsAndResults);
            JFreeChart chart = createChart(rangedTrend);
            ChartUtil.generateGraph(request, response, chart, 400, 200);
        }
    }

    List<BuildAndResults> retrieveExistingBuildsAndResults(AbstractBuild<?, ?> startingBuild)
    {
        List<BuildAndResults> buildsAndResults = new ArrayList<BuildAndResults>();
        buildsAndResults.add(new BuildAndResults(startingBuild, getResults()));

        AbstractBuild<?, ?> previousBuild = getPreviousBuild(startingBuild);
        while (previousBuild != null)
        {
            AbstractBuildReport action = previousBuild.getAction(getClass());
            if (action != null)
            {
                buildsAndResults.add(new BuildAndResults(previousBuild, action.getResults()));
            }
            previousBuild = getPreviousBuild(previousBuild);
        }
        return buildsAndResults;
    }

    /**
     * Creates a new JFreeChart based on the specified RangedTrend.
     *
     * @return the JFreeChart chart
     */
    JFreeChart createChart(RangedTrend rangedTrend)
    {
        return m_reportBuilder.createGraph(rangedTrend);
    }

    /**
     * Returns the previous build of the given {@link AbstractBuild}.
     * @param build AbstractBuild
     * @return AbstractBuild or {@code null}
     */
    AbstractBuild<?, ?> getPreviousBuild(AbstractBuild<?, ?> build)
    {
        return build.getPreviousBuild();
    }

    @Override
    public String getGraphName()
    {
        return PluginImpl.GRAPH_NAME;
    }

    public String getIconFileName()
    {
        return PluginImpl.ICON_FILE_NAME;
    }

    public String getDisplayName()
    {
        return PluginImpl.DISPLAY_NAME;
    }

    public String getUrlName()
    {
        return PluginImpl.URL;
    }

    ReportBuilder getReportBuilder() {
        return m_reportBuilder;
    }

    CostDetailBuilder getDetailBuilder()
    {
        return m_detailBuilder;
    }
}
