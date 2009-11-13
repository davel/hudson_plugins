/*******************************************************************************
* Copyright (c) 2009 Thales Corporate Services SAS                             *
* Author : Gregory Boissinot                                                   *
*                                                                              *
* Permission is hereby granted, free of charge, to any person obtaining a copy *
* of this software and associated documentation files (the "Software"), to deal*
* in the Software without restriction, including without limitation the rights *
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
* copies of the Software, and to permit persons to whom the Software is        *
* furnished to do so, subject to the following conditions:                     *
*                                                                              *
* The above copyright notice and this permission notice shall be included in   *
* all copies or substantial portions of the Software.                          *
*                                                                              *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
* THE SOFTWARE.                                                                *
*******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Shell;
import hudson.tasks.junit.TestResultAction;
import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;
import com.thalesgroup.hudson.plugins.xunit.types.BoostTestType;
import com.thalesgroup.hudson.plugins.xunit.types.CustomType;

import java.util.ArrayList;
import java.util.List;


public class XUnitPublisherIntegrationTest extends HudsonTestCase {

    public void testPeformAnUnstableTest() throws Exception {

        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);

        String boostFileName= "boosttestunstable.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("touch "+boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        //Build status
        assertBuildStatus(Result.UNSTABLE,build);

        //Build log
        StringBuffer expectedLog = new StringBuffer();
        expectedLog.append("[xUnit] Starting to record.\r\n");
        expectedLog.append("[xUnit] [Boost Test Library] - Use the embedded style sheet.\r\n");
        expectedLog.append("[xUnit] [Boost Test Library] - Processing 1 files with the pattern '" + pattern + "' relative to '" + build.getWorkspace().getRemote() + "'.\r\n");
        expectedLog.append("[xUnit] Setting the build status to UNSTABLE\r\n");
        expectedLog.append("[xUnit] Stopping recording.");
        assertLogContains(expectedLog.toString(), build);
    }

   public void testCustomToolWithCustomStyleSheet() throws Exception {

        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
        String cpptestResultFileName= "cpptestresult.xml";
        files.add(new SingleFileSCM(cpptestResultFileName, getClass().getResource(cpptestResultFileName)));
        String cpptestStyleSheet = "cpptest-to-junit.xsl";
        files.add(new SingleFileSCM(cpptestStyleSheet, getClass().getResource(cpptestStyleSheet)));
        project.setScm(new MultiFileSCM(files));

        project.getBuildersList().add(new Shell("touch "+cpptestResultFileName));
        String pattern = cpptestResultFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new CustomType(pattern, cpptestStyleSheet)}));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        //Build status
        assertBuildStatus(Result.UNSTABLE,build);

        //Build log
        StringBuffer expectedLog = new StringBuffer();
        expectedLog.append("[xUnit] Starting to record.\r\n");
        expectedLog.append("[xUnit] [Custom Tool] - Use the style sheet found into the workspace.\r\n");
        expectedLog.append("[xUnit] [Custom Tool] - Processing 1 files with the pattern '" + pattern + "' relative to '" + build.getWorkspace().getRemote() + "'.\r\n");
        expectedLog.append("[xUnit] Setting the build status to UNSTABLE\r\n");
        expectedLog.append("[xUnit] Stopping recording.");
        assertLogContains(expectedLog.toString(), build);
    }


    public void testPreviousFailedWithFailedTests() throws Exception {
               FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName= "boosttestunstable.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("wrong command"));
        project.getBuildersList().add(new Shell("touch "+boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.FAILURE,build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2,result.getTotalCount());
        assertEquals(1,result.getFailCount());
    }


    public void testPreviousFailedWithOnlySuccess() throws Exception {
               FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName= "boosttestsuccess.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("wrong command"));
        project.getBuildersList().add(new Shell("touch "+boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.FAILURE,build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(1,result.getTotalCount());
        assertEquals(0,result.getFailCount());
    }


    public void testPreviousFailedWithErrors() throws Exception {
               FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boosttesterror= "boosttesterror.xml";
        files.add(new SingleFileSCM(boosttesterror, getClass().getResource(boosttesterror)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("wrong command"));
        project.getBuildersList().add(new Shell("touch "+boosttesterror));
        String pattern = boosttesterror;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.FAILURE,build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2,result.getTotalCount());
        assertEquals(2,result.getFailCount());
    }

    public void testPreviousSuccessWithOnlySuccess() throws Exception {
               FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName= "boosttestsuccess.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("echo SUCCESS"));
        project.getBuildersList().add(new Shell("touch "+boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS,build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(1,result.getTotalCount());
        assertEquals(0,result.getFailCount());
    }

    public void testPreviousSuccessWithFailedTests() throws Exception {
               FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName= "boosttestunstable.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("echo SUCCESS"));
        project.getBuildersList().add(new Shell("touch "+boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.UNSTABLE,build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2,result.getTotalCount());
        assertEquals(1,result.getFailCount());
    }

    public void testPreviousSuccessWithErrors() throws Exception {
               FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boosttesterror= "boosttesterror.xml";
        files.add(new SingleFileSCM(boosttesterror, getClass().getResource(boosttesterror)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("echo SUCCESS"));
        project.getBuildersList().add(new Shell("touch "+boosttesterror));
        String pattern = boosttesterror;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.UNSTABLE,build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2,result.getTotalCount());
        assertEquals(2,result.getFailCount());
    }

}
