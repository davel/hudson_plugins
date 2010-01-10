package edu.ucsc.sep;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: cflewis
 * Date: Jan 9, 2010
 * Time: 5:08:08 PM
 */
public class SepPublisher extends Recorder {
    @Override
    public boolean needsToRunAfterFinalized() {
        return super.needsToRunAfterFinalized();
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener)
            throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new SepProjectAction();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }
}
