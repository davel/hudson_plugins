package hudson.plugins.testabilityexplorer.helpers;

import hudson.plugins.testabilityexplorer.parser.XmlStatisticsParser;
import hudson.plugins.testabilityexplorer.parser.selectors.DefaultConverterSelector;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import org.apache.commons.lang.SystemUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;

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
        ReportParseDelegate parseDelegate = new ReportParseDelegate("foo.xml", 100);
        
        File notExistent = new File(SystemUtils.getJavaIoTmpDir(), "nonExistentDir");
        assertFalse(notExistent.exists());

        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
        BuildProxy buildProxy = createBuildProxy(notExistent, new XmlStatisticsParser(new DefaultConverterSelector()), reportBuilder);
        parseDelegate.getFilesToParse(buildProxy);
    }


}
