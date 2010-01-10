package edu.ucsc.soe.sep;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: cflewis
 * Date: Jan 9, 2010
 * Time: 5:08:08 PM
 */
public class SepRecorder extends Recorder {
    private final String url;

    @DataBoundConstructor
    public SepRecorder(String url) {
        System.out.println("Got url " + url);
        this.url = url;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return false;
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener)
            throws InterruptedException, IOException {
        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new SepProjectAction(project, this);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    public String getUrl() {
        System.out.println("Sending back url " + url);
        return this.url;
    }
}
