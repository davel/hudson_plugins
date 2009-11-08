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

package com.thalesgroup.hudson.plugins.copyarchiver;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CopyArchiver extends Notifier implements Serializable {

    private String sharedDirectoryPath;

    private boolean useTimestamp;

    private String datePattern;

    private boolean flatten;

    private List<ArchivedJobEntry> archivedJobList = new ArrayList<ArchivedJobEntry>();

    public String getSharedDirectoryPath() {
        return sharedDirectoryPath;
    }

    public void setSharedDirectoryPath(String sharedDirectoryPath) {
        this.sharedDirectoryPath = sharedDirectoryPath;
    }

    public boolean isUseTimestamp() {
        return useTimestamp;
    }

    public void setUseTimestamp(boolean useTimestamp) {
        this.useTimestamp = useTimestamp;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public List<ArchivedJobEntry> getArchivedJobList() {
        return archivedJobList;
    }

    public void setArchivedJobList(List<ArchivedJobEntry> archivedJobList) {
        this.archivedJobList = archivedJobList;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public static final class CopyArchiverDescriptor extends BuildStepDescriptor<Publisher> {

        //CopyOnWriteList
        private List<AbstractProject> jobs;

        public CopyArchiverDescriptor() {
            super(CopyArchiverPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Aggregate the archived artifacts";
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            CopyArchiverPublisher pub = new CopyArchiverPublisher();
            req.bindParameters(pub, "copyarchiver.");
            List<ArchivedJobEntry> archivedJobEntries = req.bindParametersToList(ArchivedJobEntry.class, "copyarchiver.entry.");
            for (ArchivedJobEntry archivedJobEntry : archivedJobEntries) {
                AbstractProject curProj = (AbstractProject) Hudson.getInstance().getItem(archivedJobEntry.jobName);
                archivedJobEntry.job = curProj;
            }
            pub.getArchivedJobList().addAll(archivedJobEntries);
            return pub;
        }


        @Override
        public String getHelpFile() {
            return "/plugin/copyarchiver/help.html";
        }


        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public List<AbstractProject> getJobs() {
            return Hudson.getInstance().getItems(AbstractProject.class);
        }

    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           final BuildListener listener) throws InterruptedException, IOException {
        return copyArchiverPublisher.perform(build, launcher, listener);
    }


    private transient CopyArchiverPublisher copyArchiverPublisher;


    private Object readResolve() {
        copyArchiverPublisher = new CopyArchiverPublisher();
        copyArchiverPublisher.setSharedDirectoryPath(sharedDirectoryPath);
        copyArchiverPublisher.setUseTimestamp(useTimestamp);
        copyArchiverPublisher.setDatePattern(datePattern);
        copyArchiverPublisher.setFlatten(flatten);
        copyArchiverPublisher.setDeleteShared(true);

        for (ArchivedJobEntry archivedJobEntry : archivedJobList) {
            if (archivedJobEntry.jobName != null) {
                AbstractProject proj = (AbstractProject) Hudson.getInstance().getItem(archivedJobEntry.jobName);
                if (proj == null) {
                    archivedJobList.remove(archivedJobEntry);
                } else {
                    archivedJobEntry.job = proj;
                    archivedJobEntry.jobName = null;
                }
            }
        }
        copyArchiverPublisher.setArchivedJobList(archivedJobList);

        return copyArchiverPublisher;
    }

}
