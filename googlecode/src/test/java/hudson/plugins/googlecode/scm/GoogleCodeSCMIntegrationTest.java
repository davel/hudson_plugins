package hudson.plugins.googlecode.scm;

import hudson.matrix.MatrixProject;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;

import org.jvnet.hudson.test.HudsonTestCase;

public class GoogleCodeSCMIntegrationTest extends HudsonTestCase {

    /**
     * Asserts that google code SCM works with Matrix projects.
     * @throws Exception thrown if problem
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