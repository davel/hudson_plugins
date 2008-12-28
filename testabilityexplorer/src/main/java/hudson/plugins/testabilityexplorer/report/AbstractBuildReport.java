package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.charts.BuildAndResults;
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

    static final CostTemplate EXCELLENT_COST_TEMPLATE = new CostTemplate()
    {
        public int getCost(Statistic statistic)
        {
            return statistic.getCostSummary().getExcellent();
        }
    };

    static final CostTemplate GOOD_COST_TEMPLATE = new CostTemplate()
    {
        public int getCost(Statistic statistic)
        {
            return statistic.getCostSummary().getGood();
        }
    };

    static final CostTemplate POOR_COST_TEMPLATE = new CostTemplate()
    {
        public int getCost(Statistic statistic)
        {
            return statistic.getCostSummary().getNeedsWork();
        }
    };

    public AbstractBuildReport(Collection<Statistic> results, ReportBuilder reportBuilder) {
        m_results = results;
        m_reportBuilder = reportBuilder;
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
            CategoryDataset categoryDataset = buildClassesTrendDataSet(buildsAndResults);
            ChartUtil.generateGraph(request, response, createChart(categoryDataset), 800, 600);  // todo: calculate height
        }
        else
        {
            CategoryDataset categoryDataset = buildOverallTrendDataSet(buildsAndResults);
            ChartUtil.generateGraph(request, response, createChart(categoryDataset), 400, 200);  // todo: calculate height
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
     * Creates a new JFreeChart based on the specified CategoryDataset.
     *
     * @return the JFreeChart
     */
    JFreeChart createChart(CategoryDataset categoryDataset)
    {
        return m_reportBuilder.createGraph(categoryDataset);
    }

    /**
     * Builds and returns a {@link CategoryDataset} that will represent the classes testability trend.
     *
     * @return CategoryDataset
     */
    CategoryDataset buildClassesTrendDataSet(List<BuildAndResults> buildsAndResults)
    {
        final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (BuildAndResults buildAndResults : buildsAndResults)
        {
            AbstractBuild<?, ?> build = buildAndResults.getBuild();
            Collection<Statistic> results = buildAndResults.getStatistics();

            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
            dsb.add(summarizeCost(results, EXCELLENT_COST_TEMPLATE), "excellent", label);
            dsb.add(summarizeCost(results, GOOD_COST_TEMPLATE), "good", label);
            dsb.add(summarizeCost(results, POOR_COST_TEMPLATE), "need work", label);
        }
        return dsb.build();
    }

    /**
     * Builds and returns a {@link CategoryDataset} that will represent the overall testability trend.
     * @return CategoryDataset
     */
    CategoryDataset buildOverallTrendDataSet(List<BuildAndResults> buildsAndResults)
    {
        final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        final CostTemplate totalCostTemplate = new CostTemplate()
        {
            public int getCost(Statistic statistic)
            {
                return statistic.getCostSummary().getTotal();
            }
        };

        for (BuildAndResults buildAndResults : buildsAndResults)
        {
            AbstractBuild<?, ?> build = buildAndResults.getBuild();
            Collection<Statistic> results = buildAndResults.getStatistics();

            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
            dsb.add(summarizeCost(results, totalCostTemplate), "overall", label);
        }

        return dsb.build();
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

    /**
     * Returns a cost summary using the given collection of Statistic's and a {@link CostTemplate}.
     * @param statistics Collections of Statistic
     * @param template a CostTemplate
     * @return summed up cost as int
     */
    private int summarizeCost(Collection<Statistic> statistics, CostTemplate template)
    {
        int cost = 0;
        for (Statistic statistic : statistics)
        {
            cost += template.getCost(statistic);
        }
        return cost;
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
}
