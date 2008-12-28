package hudson.plugins.testabilityexplorer.helpers;

import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.report.BuildIndividualReport;
import hudson.plugins.testabilityexplorer.report.CostDetailBuilder;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.apache.commons.lang.SystemUtils;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

/**
 * Tests the {@link BuildProxy} class.
 *
 * @author reik.schatz
 */
@Test
public class BuildProxyTest extends PluginBaseTest
{
    public void testProxy()
    {
        FilePath path = new FilePath(SystemUtils.getJavaIoTmpDir());

        StatisticsParser statisticsParser = mock(StatisticsParser.class);

        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());

        BuildProxy buildProxy = new BuildProxy(path, statisticsParser, new CostDetailBuilder(), reportBuilder);

        assertEquals(path, buildProxy.getModuleRoot());
        assertTrue(statisticsParser == buildProxy.getStatisticsParser());
        assertEquals(reportBuilder, buildProxy.getReportBuilder());

        Collection<Statistic> results = createStatistics();
        BuildIndividualReport abstractBuildAction = new BuildIndividualReport(results, reportBuilder, new CostDetailBuilder());
        buildProxy.addAction(abstractBuildAction);

        assertNull(buildProxy.getResult());
        buildProxy.setResult(Result.SUCCESS);
        assertNotNull(buildProxy.getResult());

        assertNull(abstractBuildAction.getBuild());
        AbstractBuild<?,?> someBuild = createBuild(15, GregorianCalendar.getInstance());
        buildProxy.updateBuild(someBuild);
        assertNotNull(abstractBuildAction.getBuild());
        assertTrue(someBuild == abstractBuildAction.getBuild());
    }
}
