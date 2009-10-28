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

package com.thalesgroup.hudson.plugins.jobrevision;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.LogTaskListener;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.logging.Level;
import java.util.logging.Logger;


public class JobRevisionIntegrationTest extends HudsonTestCase {

    private static final Logger LOGGER = Logger.getLogger(JobRevisionIntegrationTest.class.getName());

    public void testEnv() throws Exception {

        FreeStyleProject project = createFreeStyleProject();
        String confRevision = "3.5";
        project.addProperty(new JobRevision(confRevision));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        EnvVars envVars = build.getEnvironment(new LogTaskListener(LOGGER, Level.INFO));
        assertBuildStatus(Result.SUCCESS, build);
        assertEquals(confRevision, envVars.get(JobRevisionEnvironmentAction.VAR_JOB_REVISION_NAME));
    }

}
