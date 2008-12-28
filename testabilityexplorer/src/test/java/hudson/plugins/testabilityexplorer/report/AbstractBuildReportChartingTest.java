package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.plugins.testabilityexplorer.PluginImpl;
import hudson.plugins.testabilityexplorer.report.charts.BuildAndResults;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.model.AbstractBuild;
import org.testng.annotations.Test;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.JFreeChart;

import static org.testng.Assert.*;

import java.util.*;

/**
 * Tests the TestabilityChartBuilder.
 */
@Test
public class AbstractBuildReportChartingTest extends PluginBaseTest
{
    public void testCreateChart()
    {
        Random randomGenerator = new Random();
        List<BuildAndResults> buildsAndResults = new ArrayList<BuildAndResults>();
        for (int i = 0; i < 10; i++)
        {
            AbstractBuild<?,?> build = createBuild(i + 1, GregorianCalendar.getInstance());

            int excellent = randomGenerator.nextInt(10);
            int good = randomGenerator.nextInt(10);
            int needWork = randomGenerator.nextInt(10);
            int total = randomGenerator.nextInt(200);
            CostSummary costSummary = new CostSummary(excellent, good, needWork, total);
            Collection<Statistic> stats = createStatistics(false, costSummary);

            buildsAndResults.add(new BuildAndResults(build, stats));
        }

        AbstractBuildReport abstractBuildReport = createAbstractBuildReport();
        String displayName = abstractBuildReport.getDisplayName();
        assertEquals(displayName, PluginImpl.DISPLAY_NAME);
        String graphName = abstractBuildReport.getGraphName();
        assertEquals(graphName, PluginImpl.GRAPH_NAME);
        String iconFileName = abstractBuildReport.getIconFileName();
        assertEquals(iconFileName, PluginImpl.ICON_FILE_NAME);
        String urlName = abstractBuildReport.getUrlName();
        assertEquals(urlName, PluginImpl.URL);

        CategoryDataset classesCategoryDataset = abstractBuildReport.buildClassesTrendDataSet(buildsAndResults);
        assertNotNull(classesCategoryDataset);
        assertEquals(classesCategoryDataset.getRowCount(), 3);
        assertEquals(classesCategoryDataset.getColumnCount(), 10);

        JFreeChart chartClasses = abstractBuildReport.createChart(classesCategoryDataset);
        assertNotNull(chartClasses);

        CategoryDataset overallCategoryDataset = abstractBuildReport.buildOverallTrendDataSet(buildsAndResults);
        assertNotNull(overallCategoryDataset);
        assertEquals(overallCategoryDataset.getRowCount(), 1);
        assertEquals(overallCategoryDataset.getColumnCount(), 10);

        JFreeChart chartOverall = abstractBuildReport.createChart(overallCategoryDataset);
        assertNotNull(chartOverall);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalid()
    {
        AbstractBuildReport abstractBuildReport = createAbstractBuildReport();
        abstractBuildReport.createChart(null);
    }

    public void testRetrieveExistingBuildsAndResults()
    {
        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());

        Collection<Statistic> results = createStatistics();

        AbstractBuild<?,?> build = createBuild(1, GregorianCalendar.getInstance());
        AbstractBuildReport abstractBuildReport = new BuildIndividualReport(results, reportBuilder, new CostDetailBuilder())
        {
            @Override
            AbstractBuild<?, ?> getPreviousBuild(AbstractBuild<?, ?> build)
            {
                return null;
            }
        };
        List items = abstractBuildReport.retrieveExistingBuildsAndResults(build);
        assertEquals(items.size(), 1);
    }

    public void testCostTemplates()
    {
        CostSummary costSummary = new CostSummary(1, 2, 3, 2);
        Statistic statistic = new ArrayList<Statistic>(createStatistics(false, costSummary)).get(0);

        CostTemplate excellent = AbstractBuildReport.EXCELLENT_COST_TEMPLATE;
        assertEquals(excellent.getCost(statistic), 1);

        CostTemplate good = AbstractBuildReport.GOOD_COST_TEMPLATE;
        assertEquals(good.getCost(statistic), 2);

        CostTemplate poor = AbstractBuildReport.POOR_COST_TEMPLATE;
        assertEquals(poor.getCost(statistic), 3);
    }

    private AbstractBuildReport createAbstractBuildReport()
    {
        ReportBuilder reportBuilder = new TestabilityReportBuilder(null, new TemporaryHealthCalculator());

        Collection<Statistic> results = createStatistics();
        return new BuildIndividualReport(results, reportBuilder, new CostDetailBuilder());
    }
}
