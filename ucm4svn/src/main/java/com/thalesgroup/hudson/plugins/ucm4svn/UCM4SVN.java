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

package com.thalesgroup.hudson.plugins.ucm4svn;

import com.thalesgroup.hudson.plugins.ucm4svn.util.UCM4SVNLog;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;


public class UCM4SVN extends SCM implements Serializable {

    private String projectName;

    @DataBoundConstructor
    public UCM4SVN(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> abstractBuild, Launcher launcher, TaskListener taskListener) throws IOException, InterruptedException {
        return null;
    }

    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> abstractProject, Launcher launcher, FilePath filePath, TaskListener taskListener, SCMRevisionState scmRevisionState) throws IOException, InterruptedException {
        return null;
    }

    public boolean checkout(AbstractBuild abstractBuild, Launcher launcher, FilePath filePath, BuildListener buildListener, File file) throws IOException, InterruptedException {
        UCM4SVNLog.log(buildListener, "CHECKOUT");
        return true;
    }

    public ChangeLogParser createChangeLogParser() {
        return null;
    }

    @Override
    public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        return false;
    }

    @Extension
    public static final class DescriptorImpl extends SCMDescriptor<UCM4SVN> {

        public DescriptorImpl() {
            super(UCM4SVN.class, null);
            load();
        }

        public String getDisplayName() {
            return Messages.ucm4svn_Publisher_Name();
        }

    }
}
