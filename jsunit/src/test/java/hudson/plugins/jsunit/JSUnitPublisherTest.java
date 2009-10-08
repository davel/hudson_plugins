package hudson.plugins.jsunit;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.tasks.test.TestResultProjectAction;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class JSUnitPublisherTest {

    private Mockery classContext;
    private AbstractProject project;

    @Before
    public void setUp() throws Exception {
        classContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        project = classContext.mock(AbstractProject.class);
    }

    @Test
    public void testGetTestResultsPattern() {
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", true, false, false);
        assertEquals("The test results pattern is incorrect", publisher.getTestResultsPattern(), "**/*.xml");
    }

    @Test
    public void testGetDebug() {
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", true, false, false);
        assertTrue("Debug is incorrect", publisher.getDebug());
        publisher = new JSUnitPublisher("**/*.xml", false, false, false);
        assertFalse("Debug is incorrect", publisher.getDebug());
    }

    @Test
    public void testDisabledDebug() {
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", false, true, true);
        assertFalse("Debug is incorrect", publisher.getDebug());
        assertFalse("KeepJunitReports() is incorrect", publisher.getKeepJunitReports());
        assertFalse("SkipJunitArchiver() is incorrect", publisher.getSkipJunitArchiver());
    }

    @Test
    public void testGetKeepJunitReports() {
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", true, true, false);
        assertTrue("KeepJunitReports() is incorrect", publisher.getKeepJunitReports());
        publisher = new JSUnitPublisher("**/*.xml", true, false, false);
        assertFalse("KeepJunitReports() is incorrect", publisher.getKeepJunitReports());
    }

    @Test
    public void testGetSkipJunitArchiver() {
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", true, false, true);
        assertTrue("SkipJunitArchiver() is incorrect", publisher.getSkipJunitArchiver());
        publisher = new JSUnitPublisher("**/*.xml", true, false, false);
        assertFalse("SkipJunitArchiver() is incorrect", publisher.getSkipJunitArchiver());
    }

    @Test
    public void testGetProjectActionProjectReusing() {
        classContext.checking(new Expectations() {
            {
                one(project).getAction(with(equal(TestResultProjectAction.class))); will(returnValue(new TestResultProjectAction(project)));
            }
        });
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", false, false, true);
        Action projectAction = publisher.getProjectAction(project);
        assertNull("The action was not null", projectAction);
    }

    @Test
    public void testGetProjectActionProject() {
        classContext.checking(new Expectations() {
            {
                one(project).getAction(with(equal(TestResultProjectAction.class))); will(returnValue(null));
            }
        });
        JSUnitPublisher publisher = new JSUnitPublisher("**/*.xml", false, false, true);
        Action projectAction = publisher.getProjectAction(project);
        assertNotNull("The action was null", projectAction);
        assertEquals("The action type is incorrect", TestResultProjectAction.class, projectAction.getClass());
    }
}
