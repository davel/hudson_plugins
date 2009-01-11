package hudson.plugins.testabilityexplorer.publisher;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.apache.commons.lang.SystemUtils;
import hudson.maven.MavenReporterDescriptor;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.FilePath;
import hudson.plugins.testabilityexplorer.PluginImpl;
import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.parser.XmlStatisticsParser;
import hudson.plugins.testabilityexplorer.helpers.ParseDelegate;
import hudson.plugins.testabilityexplorer.helpers.ReportParseDelegate;
import hudson.plugins.testabilityexplorer.report.ProjectIndividualReport;
import hudson.plugins.testabilityexplorer.report.CostDetailBuilder;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;

import java.io.File;

/**
 * Tests the MavenPublisher.
 *
 * @author reik.schatz
 */
@Test
public class MavenPublisherTest
{
    public void testMavenPublisher()
    {
        MavenPublisher publisher = new MavenPublisher("report.xml", "100", "50");
        String filenamePattern = publisher.getReportFilenamePattern();
        assertEquals(filenamePattern, "report.xml");
        int threshold = publisher.getThreshold();
        assertTrue(threshold == 100);
        int perClassThreshold = publisher.getPerClassThreshold();
        assertTrue(perClassThreshold == 50);

        File root = SystemUtils.getJavaIoTmpDir();
        FilePath rootPath = new FilePath(root);

        final AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getRootDir()).toReturn(root);
        stub(project.getModuleRoot()).toReturn(rootPath);

        Action action = publisher.getProjectAction(null);
        assertTrue(action instanceof ProjectIndividualReport);

        CostDetailBuilder detailBuilder = publisher.newDetailBuilder();
        assertNotNull(detailBuilder);
        ParseDelegate parseDelegate = publisher.newParseDelegate();
        assertTrue(parseDelegate instanceof ReportParseDelegate);
        ReportBuilder reportBuilder = publisher.newReportBuilder();
        assertTrue(reportBuilder instanceof TestabilityReportBuilder);
        StatisticsParser statisticsParser = publisher.newStatisticsParser();
        assertTrue(statisticsParser instanceof XmlStatisticsParser);

        MavenReporterDescriptor mavenReporterDescriptor = publisher.getDescriptor();
        String displayName = mavenReporterDescriptor.getDisplayName();
        assertEquals(displayName, "Publish " + PluginImpl.DISPLAY_NAME);
        String helpFile = mavenReporterDescriptor.getHelpFile();
        assertEquals(helpFile, "help");
        String configPage = mavenReporterDescriptor.getConfigPage();
        assertEquals(configPage, "/hudson/plugins/testabilityexplorer/publisher/MavenPublisher/config.jelly");
    }
}
