package hudson.plugins.testabilityexplorer.report.health;

import hudson.model.HealthReport;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.PluginBaseTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tests health reporting.
 *
 * @author reik.schatz
 */
@Test
public class ReportBuilderTest extends PluginBaseTest
{
    public void testHealthReporting()
    {
        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());

        {
            CostSummary costSummary = new CostSummary(1, 1, 1, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 33);    // 2 of 3 classes are not excellent
        }

        {
            CostSummary costSummary = new CostSummary(2, 0, 1, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 66);    // 1 of 3 classes are not excellent
        }

        {
            CostSummary costSummary = new CostSummary(3, 0, 0, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 100);    // 3 of 3 classes are not excellent
        }

        {
            CostSummary costSummary = new CostSummary(2, 1, 1, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 50);    // 2 of 4 classes are not excellent
        }

        {
            CostSummary costSummary = new CostSummary(0, 2, 3, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 0);    // 0 of 5 classes are not excellent
        }
        
        {
            CostSummary costSummary = new CostSummary(0, 0, 0, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 0);    // 0 of 0 classes are not excellent (maybe should be 100%)
        }

        {
            CostSummary costSummary = new CostSummary(11, 4, 32, 40);
            Collection<Statistic> stats = createStatistics(false, costSummary);
            assertEquals(stats.size(), 1);

            HealthReport report = reportBuilder.computeHealth(stats);
            assertEquals(report.getScore(), 23);    // 11 of 47 classes are not excellent
        }
    }
}
