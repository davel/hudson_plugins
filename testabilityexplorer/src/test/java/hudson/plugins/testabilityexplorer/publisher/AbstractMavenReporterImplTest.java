package hudson.plugins.testabilityexplorer.publisher;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.model.AbstractProject;
import hudson.model.Result;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.io.IOUtils;
import static org.mockito.Mockito.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.io.*;

import hudson.plugins.testabilityexplorer.PluginBaseTest;

/**
 * Tests the AbstractMavenReporterImpl.
 *
 * @author reik.schatz
 */
@Test
public class AbstractMavenReporterImplTest extends PluginBaseTest
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

    public void testUnstableBuilds() throws Throwable
    {
        MavenBuild mavenBuild = mock(MavenBuild.class);
        stub(mavenBuild.isBuilding()).toReturn(true);

        final File root = getReport().getParentFile();
        final FilePath rootPath = new FilePath(root);
        stub(mavenBuild.getProject()).toReturn(null);

        MavenPublisher publisher = new MavenPublisher(REPORT_FILE_NAME, "10", "50")
        {
            @Override
            FilePath getModuleRoot(AbstractProject project)
            {
                return rootPath;
            }
        };
        MavenBuildProxy.BuildCallable<Void, IOException> callable = publisher.getBuildCallable(null);
        callable.call(mavenBuild);
        verify(mavenBuild).setResult(Result.UNSTABLE);
    }
}
