package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.plugins.testabilityexplorer.PluginImpl;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Tests the {@link BuildIndividualReport}.
 *
 * @author reik.schatz
 */
@Test
public class TestabilityExplorerBuildIndividualReportTest extends PluginBaseTest
{
    public void testReport()
    {
        Collection<Statistic> statistics = createStatistics();

        Random random = new Random();
        int health = random.nextInt(100);
        HealthReport healthReport = new HealthReport(health, "Testing");

        ReportBuilder healthReportBuilder = createMock(ReportBuilder.class);
        expect(healthReportBuilder.computeHealth(isA(Collection.class)))
                .andReturn(healthReport)
                .times(1);
        replay(healthReportBuilder);

        BuildIndividualReport report = new BuildIndividualReport(statistics, healthReportBuilder, new CostDetailBuilder());
        HealthReport buildHealth = report.getBuildHealth();
        assertNotNull(buildHealth);
        assertEquals(health, buildHealth.getScore());
        assertEquals("Testing", buildHealth.getDescription());

        AbstractBuild<?, ?> build = createBuild(15, GregorianCalendar.getInstance());
        report.setBuild(build);

        assertNotNull(report.getBuild());
        assertFalse(report.isFloatingBoxActive());
        assertFalse(report.isGraphActive());
        assertEquals(PluginImpl.GRAPH_NAME, report.getGraphName());
        assertEquals(report.getSummary(), " (Total: 56)");
    }
}
