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
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;


public class UCM4SVN extends SCM implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(UCM4SVN.class.getName());
            

    private String projectName;

    @DataBoundConstructor
    public UCM4SVN(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> abstractBuild, Launcher launcher, TaskListener taskListener) throws IOException, InterruptedException {
        return null;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> abstractProject, Launcher launcher, FilePath filePath, TaskListener taskListener, SCMRevisionState scmRevisionState) throws IOException, InterruptedException {
        return null;
    }

    @Override
    public boolean checkout(AbstractBuild abstractBuild, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        UCM4SVNLog.log(listener, "CHECKOUT");

        generateLogFile("il y a  des changes", changelogFile);

        return true;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new ChangeLogParser() {

            //Note : This method is called only if the changelog file is not empty!!
            @Override
            public ChangeLogSet<? extends hudson.scm.ChangeLogSet.Entry> parse(final AbstractBuild build, final File changelogFile)
                    throws IOException, SAXException {
                return new UCM4SVNChangeLogSet(build, changelogFile);
            }
        };
    }

    @Override
    public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        return false;
    }

    @Extension
    public static final class DescriptorImpl extends SCMDescriptor<UCM4SVN> {

        @CopyOnWrite
        private volatile UCM4SVNInstallation installation = null;

        public DescriptorImpl() {
            super(UCM4SVN.class, null);
            load();
        }

        public String getDisplayName() {
            return Messages.ucm4svn_Publisher_Name();
        }

        public UCM4SVNInstallation getInstallation() {
            return installation;
        }

        public void setInstallation(UCM4SVNInstallation installation) {
            this.installation = installation;
            save();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) {       
            setInstallation(new UCM4SVNInstallation(json.getString("serverName"), json.getString("username"), json.getString("password")));
            return true;
        }

    }

    protected void generateLogFile(String contents, File changelogFile) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(changelogFile);
            writer.write(contents);
            writer.flush();
        } catch (IOException ioe) {
            LOGGER.warning("Unexpected exception writing Dimensions change log file: " + ioe.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
                LOGGER.warning("Could not close UCM4SVN change log after writing: " + ioe.getMessage());
            }
        }
    }

}
