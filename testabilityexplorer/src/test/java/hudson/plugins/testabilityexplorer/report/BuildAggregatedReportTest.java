package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.maven.AggregatableAction;
import hudson.maven.MavenBuild;
import hudson.model.AbstractBuild;
import static org.mockito.Mockito.*;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Collection;

/**
 * Tests the BuildAggregatedReport.
 *
 * @author reik.schatz
 */
@Test
public class BuildAggregatedReportTest extends PluginBaseTest
{
    public void testGetIndividualActionType()
    {
        Collection<Statistic> statistics = createStatistics();

        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
        CostDetailBuilder costDetailBuilder = new CostDetailBuilder();
        BuildAggregatedReport buildAggregatedReport = new BuildAggregatedReport(null, statistics, reportBuilder, costDetailBuilder);
        Class<? extends AggregatableAction> actionType = buildAggregatedReport.getIndividualActionType();
        assertTrue(BuildIndividualReport.class.isAssignableFrom(actionType));
    }

    public void testUpdate()
    {
        CostSummary costSummary = new CostSummary(12, 14, 43, 43);
        Collection<Statistic> stats1 = createStatistics(false, costSummary);

        Collection<Statistic> stats2 = createStatistics();
        BuildIndividualReport report = new BuildIndividualReport(stats1, null, new CostDetailBuilder());
        AbstractBuild<?, ?> build1 = mock(AbstractBuild.class);
        stub(build1.toString()).toReturn("Build 1");
        report.setBuild(build1);

        MavenBuild build = mock(MavenBuild.class);
        stub(build.getAction(BuildIndividualReport.class)).toReturn(report);

        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
        CostDetailBuilder costDetailBuilder = new CostDetailBuilder();
        BuildAggregatedReport buildAggregatedReport = new BuildAggregatedReport(null, stats2, reportBuilder, costDetailBuilder);
        assertEquals(buildAggregatedReport.getResults().size(), 1);
        buildAggregatedReport.update(null, build);

        Collection<Statistic> combinedStats = buildAggregatedReport.getResults();
        assertEquals(combinedStats.size(), 2);

        MavenBuild invalidBuild = mock(MavenBuild.class);
        stub(invalidBuild.getAction(BuildIndividualReport.class)).toReturn(null);
        buildAggregatedReport.update(null, invalidBuild);
        combinedStats = buildAggregatedReport.getResults();
        assertEquals(combinedStats.size(), 2);
    }
}
