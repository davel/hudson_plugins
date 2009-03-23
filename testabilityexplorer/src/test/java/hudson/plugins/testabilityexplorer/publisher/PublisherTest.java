package hudson.plugins.testabilityexplorer.publisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import org.apache.commons.lang.SystemUtils;
import org.testng.annotations.Test;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

/**
 * Tests against the {@link Publisher} interface.
 *
 * @author reik.schatz
 */
@Test
public class PublisherTest extends PluginBaseTest {

    public void testPublisher() throws IOException, InterruptedException {
        AbstractBuild<?, ?> build = createBuild(15, GregorianCalendar.getInstance());

        File root = SystemUtils.getJavaIoTmpDir();
        FilePath rootPath = new FilePath(root);

        final AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getRootDir()).toReturn(root);
        stub(project.getModuleRoot()).toReturn(rootPath);

        Publisher publisher = new FreestylePublisher("reports.xml", "","", "100", "10") {

            protected AbstractProject getProject(AbstractBuild build) {
                return project;
            }
        };
        publisher.perform(build, null, null);
    }
}
