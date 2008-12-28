package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.plugins.testabilityexplorer.PluginImpl;
import hudson.model.AbstractProject;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.Collection;

/**
 * Tests the {@link AbstractProjectReport}.
 *
 * @author reik.schatz
 */
@Test
public class AbstractProjectReportTest extends PluginBaseTest
{
    public void testDefaultReport()
    {
        AbstractProject<?, ?> project = mock(AbstractProject.class);

        AbstractProjectReport defaultReport = new ProjectIndividualReport(project);
        assertNull(defaultReport.getApplicableBuildAction());
        assertNull(defaultReport.getDisplayName());
        assertNull(defaultReport.getIconFileName());
        assertNull(defaultReport.getUrlName());
        assertTrue(defaultReport.getResults().isEmpty());
        assertEquals(defaultReport.getTotals(), 0);
    }

    public void testTestabilityExplorerProjectIndividualReport()
    {
        AbstractProject<?, ?> project = mock(AbstractProject.class);

        final Collection<Statistic> stats = createStatistics();
        AbstractProjectReport testabilityExplorerProjectIndividualReport = new AbstractProjectReport(project)
        {
            @Override
            protected AbstractBuildReport getApplicableBuildAction()
            {
                ChartBuilder chartBuilder = new TestabilityChartBuilder();
                return new BuildIndividualReport(stats, new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator()), new CostDetailBuilder());
            }

            protected Class getBuildActionClass()
            {
                return BuildIndividualReport.class;
            }
        };

        assertEquals(testabilityExplorerProjectIndividualReport.getDisplayName(), PluginImpl.DISPLAY_NAME);
        assertEquals(testabilityExplorerProjectIndividualReport.getIconFileName(), PluginImpl.ICON_FILE_NAME);
        assertEquals(testabilityExplorerProjectIndividualReport.getUrlName(), PluginImpl.URL);
        assertEquals(testabilityExplorerProjectIndividualReport.getSearchUrl(), PluginImpl.URL);
        Collection results = testabilityExplorerProjectIndividualReport.getResults();
        assertFalse(results.isEmpty());
        assertEquals(testabilityExplorerProjectIndividualReport.getTotals(), 56);
    }
}
