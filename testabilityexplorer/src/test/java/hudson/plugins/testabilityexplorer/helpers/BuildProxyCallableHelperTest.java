package hudson.plugins.testabilityexplorer.helpers;

import hudson.plugins.testabilityexplorer.parser.XmlStatisticsParser;
import hudson.plugins.testabilityexplorer.parser.selectors.DefaultConverterSelector;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.model.BuildListener;
import hudson.FilePath;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.io.IOUtils;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

import java.io.*;
import java.util.List;

/**
 * @author reik.schatz
 */
@Test
public class BuildProxyCallableHelperTest extends PluginBaseTest
{
    private static final String REPORT_FILE_NAME = "report.xml";

    @BeforeClass
    public void setUp() throws IOException
    {
        File report = getReport();
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(report),"UTF8"));
            out.write(createReportXml());
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
        assertTrue(report.exists());
    }

    @AfterClass
    public void tearDown()
    {
        File report = getReport();
        if (report.exists())
        {
            report.delete();
        }
        assertFalse(report.exists());
    }

    protected File getReport()
    {
        return new File(SystemUtils.getJavaIoTmpDir(), REPORT_FILE_NAME);
    }

    public void testCalling() throws Exception
    {
        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
        BuildProxy buildProxy = createBuildProxy(SystemUtils.getJavaIoTmpDir(), new XmlStatisticsParser(new DefaultConverterSelector()), reportBuilder);

        ReportParseDelegate parseDelegate = new ReportParseDelegate(REPORT_FILE_NAME, 100, 80);
        List<FilePath> pathList = parseDelegate.getFilesToParse(buildProxy);
        assertNotNull(pathList);
        assertEquals(pathList.size(), 1);

        BuildListener buildListener = createMock(BuildListener.class);
        replay(buildListener);

        BuildProxyCallableHelper buildProxyCallableHelper = new BuildProxyCallableHelper(buildProxy, parseDelegate, buildListener);
        buildProxy = buildProxyCallableHelper.call();
    }
}
