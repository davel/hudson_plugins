package hudson.plugins.googlecode.scm;

import hudson.matrix.MatrixProject;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.plugins.googlecode.PluginImpl;
import hudson.scm.SCMS;

import java.io.IOException;

import org.jvnet.hudson.test.HudsonTestCase;

public class GoogleCodeSCMTest extends HudsonTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SCMS.SCMS.add(PluginImpl.GOOGLE_CODE_SCM_DESCRIPTOR);
    }
    
    @Override
    protected void tearDown() throws Exception {
        SCMS.SCMS.remove(PluginImpl.GOOGLE_CODE_SCM_DESCRIPTOR);
        super.tearDown();
    }

    protected MatrixProject createMatrixProject(String name) throws IOException {
        return (MatrixProject)hudson.createProject(MatrixProject.DESCRIPTOR,name);
    }

    /**
     * Asserts that google code SCM works with Matrix projects.
     */
    public void testGoogleCodeScmNoLongerThrowsExceptionInAMatrixProject() throws Exception {
        MatrixProject project = createMatrixProject("matrix");
        project.setScm(new GoogleCodeSCM("path2"));
        project.addProperty(new GoogleCodeProjectProperty("http://www.googlecode.com/p/mockitopp"));
        
        // Call any SCM method on the google code used to throw RuntimeException
        // when not finding the correct project
        project.getScm().getBrowser();
    }
}
