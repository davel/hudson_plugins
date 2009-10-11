package com.thalesgroup.hudson.plugins.copyarchiver;

import hudson.tasks.Publisher;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildStepDescriptor;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;


public class CopyArchiver extends Publisher implements Serializable {

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
        public Publisher newInstance(StaplerRequest req) throws FormException {
            CopyArchiverPublisher pub = new CopyArchiverPublisher();
            req.bindParameters(pub, "copyarchiver.");
            pub.getArchivedJobList().addAll(req.bindParametersToList(ArchivedJobEntry.class, "copyarchiver.entry."));
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
        copyArchiverPublisher.setArchivedJobList(archivedJobList);

        return copyArchiverPublisher;
    }

}
