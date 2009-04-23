package hudson.plugins.javancss;

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.FreeStyleBuild;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.tasks.Shell;
import hudson.tasks.BatchFile;
import hudson.model.Result;

import java.util.List;
import java.util.ArrayList;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;

public class JavaNCSSPublisherTest extends HudsonTestCase {
   
    /**
     * Verify that it works on a master.
     */
    public void testOnMaster() throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
	
        files.add(new SingleFileSCM("javancss-reports/first-report/first-report-javancss.xml",
                                    getClass().getResource("javancss-reports/first-report/first-report-javancss.xml")));
        files.add(new SingleFileSCM("javancss-reports/second-report/second-report-javancss.xml",
                                    getClass().getResource("javancss-reports/second-report/second-report-javancss.xml")));
        
        project.setScm(new MultiFileSCM(files));
	
        project.getPublishersList().add(new JavaNCSSPublisher("**/*-javancss.xml", null));
        FreeStyleBuild build1 = project.scheduleBuild2(0).get();
	
        FreeStyleBuild build2 = project.scheduleBuild2(0).get();
        System.out.println(build2.getLog());
        assertBuildStatusSuccess(build2);

    }

    /**
     * Verify that it works on a slave.
     */
    public void testOnSlave() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        DumbSlave slave = createSlave(new Label("javancss-test-slave"));
        
        project.setAssignedLabel(slave.getSelfLabel());
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
	
        files.add(new SingleFileSCM("javancss-reports/first-report/first-report-javancss.xml",
                                    getClass().getResource("javancss-reports/first-report/first-report-javancss.xml")));
        files.add(new SingleFileSCM("javancss-reports/second-report/second-report-javancss.xml",
                                    getClass().getResource("javancss-reports/second-report/second-report-javancss.xml")));
        
        project.setScm(new MultiFileSCM(files));
	
        project.getPublishersList().add(new JavaNCSSPublisher("**/*-javancss.xml", null));
        FreeStyleBuild build1 = project.scheduleBuild2(0).get();
	
        FreeStyleBuild build2 = project.scheduleBuild2(0).get();
        System.out.println(build2.getLog());
        assertBuildStatusSuccess(build2);
    }

}
