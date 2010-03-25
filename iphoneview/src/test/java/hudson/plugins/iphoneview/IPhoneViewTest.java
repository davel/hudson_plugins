package hudson.plugins.iphoneview;

import hudson.model.FreeStyleProject;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import mockit.Expectations;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Test for {@link IPhoneView}
 * 
 * @author Seiji Sogabe
 */
public class IPhoneViewTest extends HudsonTestCase {

    private IPhoneView view;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        view = new IPhoneView("iPhone");
    }

    /**
     * Test of hasJobTestResult method, of class IPhoneView.
     */
    public void testHasJobTestResult_NoJobs() throws Exception {

        new Expectations(view) {

            {
                view.getJob(anyString);
                returns(null);
            }
        };

        try {
            view.hasJobTestResult("job");
        } catch (IllegalArgumentException e) {
            //
        }
    }

    /**
     * Test of hasJobTestResult method, of class IPhoneView.
     */
    public void testHasJobTestResult_NotJob() throws Exception {

        new Expectations(view) {

            FreeStyleProject mockFreeStyleProject;

            {
                view.getJob(anyString);
                returns(mockFreeStyleProject);
            }
        };

        try {
            view.hasJobTestResult("job");
        } catch (IllegalArgumentException e) {
            //
        }
    }

    /**
     * Test of hasJobTestResult method, of class IPhoneView.
     */
    public void testHasJobTestResult_NoActions() throws Exception {

        new Expectations(view) {

            FreeStyleProject mockFreeStyleProject;

            {
                view.getJob(anyString);
                returns(mockFreeStyleProject);

                mockFreeStyleProject.getAction(TestResultProjectAction.class);
                returns(null);
            }
        };

        boolean result = view.hasJobTestResult("job");

        assertFalse(result);
    }

    /**
     * Test of hasJobTestResult method, of class IPhoneView.
     */
    public void testHasJobTestResult_NoPreviousResult() throws Exception {

        new Expectations(view) {

            FreeStyleProject mockFreeStyleProject;
            TestResultProjectAction mockTestResultProjectAction;
            AbstractTestResultAction mockTestResultAction;

            {
                view.getJob(anyString);
                returns(mockFreeStyleProject);

                mockFreeStyleProject.getAction(TestResultProjectAction.class);
                returns(mockTestResultProjectAction);

                mockTestResultProjectAction.getLastTestResultAction();
                returns(mockTestResultAction);

                mockTestResultAction.getPreviousResult();
                returns(null);
            }
        };

        boolean result = view.hasJobTestResult("job");

        assertFalse(result);
    }

    /**
     * Test of hasJobTestResult method, of class IPhoneView.
     */
    public void testHasJobTestResult_NotNullPreviousResult() throws Exception {

        new Expectations(view) {

            FreeStyleProject mockFreeStyleProject;
            TestResultProjectAction mockTestResultProjectAction;
            TestResultAction mockTestResultAction;

            {
                view.getJob(anyString);
                returns(mockFreeStyleProject);

                mockFreeStyleProject.getAction(TestResultProjectAction.class);
                returns(mockTestResultProjectAction);

                mockTestResultProjectAction.getLastTestResultAction();
                returns(mockTestResultAction);

                mockTestResultAction.getPreviousResult();
                returns(mockTestResultAction);
            }
        };

        boolean result = view.hasJobTestResult("job");

        assertTrue(result);
    }
}
