package hudson.plugins.testabilityexplorer.helpers;

import hudson.plugins.testabilityexplorer.parser.XmlStatisticsParser;
import hudson.plugins.testabilityexplorer.parser.selectors.DefaultConverterSelector;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.BuildAndResults;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.model.AbstractBuild;
import org.apache.commons.lang.SystemUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * @author reik.schatz
 */
public class ReportParseDelegateTest extends PluginBaseTest
{
    private static final String REPORT_FILE_NAME = "report.xml";

    protected File getReport()
    {
        return new File(SystemUtils.getJavaIoTmpDir(), REPORT_FILE_NAME);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testGetFilesToParse()
    {
        ReportParseDelegate parseDelegate = new ReportParseDelegate("foo.xml", 100, 80);
        
        File notExistent = new File(SystemUtils.getJavaIoTmpDir(), "nonExistentDir");
        assertFalse(notExistent.exists());

        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
        BuildProxy buildProxy = createBuildProxy(notExistent, new XmlStatisticsParser(new DefaultConverterSelector()), reportBuilder);
        parseDelegate.getFilesToParse(buildProxy);
    }

    @Test
    public void testIsSuccessful()
    {
        // total: 56, classCost: 20
        Collection<Statistic> statistics1 = createStatistics();

        ReportParseDelegate delegate1 = createParseDelegate(100, 80);
        assertTrue(delegate1.isSuccessful(statistics1));

        ReportParseDelegate delegate2 = createParseDelegate(50, 80);
        assertFalse(delegate2.isSuccessful(statistics1));

        ReportParseDelegate delegate3 = createParseDelegate(100, 10);
        assertFalse(delegate3.isSuccessful(statistics1));

        assertTrue(delegate3.isSuccessful(null));
        assertTrue(delegate3.isSuccessful(new ArrayList<Statistic>()));

        // total: highest 56, classCost: highest 20
        Collection<Statistic> statistics2 = createStatistics();
        for (int i = 0; i < 10; i++)
        {
            int excellent = 10;
            int good = 10;
            int needWork = 10;
            int total = 5 * (i + 1);

            ClassCost classCost = new ClassCost(getClass().getName(), 2 * (i + 1));
            CostSummary costSummary = new CostSummary(excellent, good, needWork, total);
            costSummary.addToCostStack(classCost);

            statistics2.addAll(createStatistics(false, costSummary));
        }
        assertEquals(statistics2.size(), 11);

        ReportParseDelegate delegate4 = createParseDelegate(100, 10);
        assertFalse(delegate4.isSuccessful(statistics2));

        ReportParseDelegate delegate5 = createParseDelegate(100, 30);
        assertTrue(delegate5.isSuccessful(statistics2));

        ReportParseDelegate delegate6 = createParseDelegate(35, 60);
        assertFalse(delegate6.isSuccessful(statistics2));
    }

    private ReportParseDelegate createParseDelegate(int overallThreshold, int perClassThreshold)
    {
        return new ReportParseDelegate("foo.xml", overallThreshold, perClassThreshold);
    }
}
