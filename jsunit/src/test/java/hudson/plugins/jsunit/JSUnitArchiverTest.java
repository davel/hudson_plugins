package hudson.plugins.jsunit;

import hudson.plugins.jsunit.TestReportTransformer;
import hudson.plugins.jsunit.JSUnitArchiver;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import hudson.AbortException;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JSUnitArchiverTest extends AbstractWorkspaceTest {

    private BuildListener buildListener;
    private Mockery context;
    private Mockery classContext;
    private TestReportTransformer transformer;
    private JSUnitArchiver jsUnitArchiver;
    private VirtualChannel virtualChannel;

    @Before
    public void setUp() throws Exception {
        super.createWorkspace();

        context = new Mockery();
        classContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        buildListener = classContext.mock(BuildListener.class);

        transformer = context.mock(TestReportTransformer.class);
        virtualChannel = context.mock(VirtualChannel.class);
    }

    @After
    public void tearDown() throws Exception {
        super.deleteWorkspace();
    }

    /*@Test
    public void testRemovalOfJunitFiles() throws Exception {
        jsUnitArchiver = new JSUnitArchiver(buildListener, "*.xml", archiver, transformer, false, false);
        workspace.createTextTempFile("jsUnit-report", ".xml", "content");
        workspace.child(JSUnitArchiver.JUNIT_REPORTS_PATH).mkdirs();
        workspace.child(JSUnitArchiver.JUNIT_REPORTS_PATH).createTextTempFile("TEST-", ".xml", "<tests>");

        context.checking(new Expectations() {
            {
                one(transformer).transform(with(any(InputStream.class)), with(any(File.class)));
                one(archiver).archive();
                will(returnValue(true));
            }
        });
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
            }
        });

        jsUnitArchiver.invoke(PARENT_FILE, virtualChannel);

        assertFalse("The temp folder still exists", workspace.child(JSUnitArchiver.JUNIT_REPORTS_PATH).exists());
        context.assertIsSatisfied();
    }*/

    @Test
    public void testTransformOfTwoReports() throws Exception {
        jsUnitArchiver = new JSUnitArchiver(buildListener, "*.xml", transformer);
        workspace.createTextTempFile("jsUnit-report", ".xml", "content");
        workspace.createTextTempFile("jsUnit-report", ".xml", "content");

        context.checking(new Expectations() {
            {
                exactly(2).of(transformer).transform(with(any(InputStream.class)), with(any(File.class)));
            }
        });
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
            }
        });
        jsUnitArchiver.invoke(parentFile, virtualChannel);

        context.assertIsSatisfied();
    }
/*
    @Test
    public void testKeepJUnitReportFiles() throws Exception {
        jsUnitArchiver = new JSUnitArchiver(buildListener, "*.xml", archiver, transformer, true, false);
        workspace.createTextTempFile("jsUnit-report", ".xml", "content");

        context.checking(new Expectations() {
            {
                one(transformer).transform(with(any(InputStream.class)), with(any(File.class)));
                one(archiver).archive();
                will(returnValue(true));
            }
        });
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
            }
        });

        jsUnitArchiver.invoke(PARENT_FILE, virtualChannel);

        assertTrue("The temp folder still exists", workspace.child(JSUnitArchiver.JUNIT_REPORTS_PATH).exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testSkipJUnitArchiver() throws Exception {
        jsUnitArchiver = new JSUnitArchiver(buildListener, "*.xml", archiver, transformer, true, true);
        workspace.createTextTempFile("jsUnit-report", ".xml", "content");

        context.checking(new Expectations() {
            {
                one(transformer).transform(with(any(InputStream.class)), with(any(File.class)));
            }
        });
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
            }
        });

        jsUnitArchiver.invoke(PARENT_FILE, virtualChannel);
        context.assertIsSatisfied();
    }*/

    @Test
    public void testNoJSUnitReports() throws Exception {
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
                one(buildListener).fatalError(with(any(String.class)));
            }
        });
        jsUnitArchiver = new JSUnitArchiver(buildListener, "*.xml", transformer);
        Boolean result = jsUnitArchiver.invoke(parentFile, virtualChannel);
        assertFalse("The archiver did not return false when it could not find any files", result);
    }
}
