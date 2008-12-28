package hudson.plugins.testabilityexplorer.report;

import hudson.model.AbstractProject;
import static org.mockito.Mockito.mock;
import org.testng.annotations.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link ProjectIndividualReport}.
 *
 * @author reik.schatz
 */
@Test
public class TestabilityExplorerProjectIndividualReportTest extends TestCase
{
    public void testReport()
    {
        AbstractProject<?, ?> project = mock(AbstractProject.class);

        ProjectIndividualReport report = new ProjectIndividualReport(project);
        Class buildActionClass = report.getBuildActionClass();
        assertNotNull(buildActionClass);
        assertTrue(buildActionClass == BuildIndividualReport.class);

        assertNotNull(report.getProject());
        assertTrue(project == report.getProject());
        assertTrue(report.isFloatingBoxActive());
        assertTrue(report.isGraphActive());
        assertNotNull(report.getGraphName());
    }
}
