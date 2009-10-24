package hudson.plugins.helpers;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.Launcher;

import java.io.IOException;

/**
 * An abstract Publisher that is designed to work with a Ghostwriter.
 *
 * @author Stephen Connolly
 * @since 28-Jan-2008 22:32:46
 */
public abstract class AbstractPublisherImpl extends Recorder {

    /**
     * Creates the configured Ghostwriter.
     *
     * @return returns the configured Ghostwriter.
     */
    protected abstract Ghostwriter newGhostwriter();

    /**
     * {@inheritDoc}
     */
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        return BuildProxy.doPerform(newGhostwriter(), build, listener);
    }

    /**
     * {@inheritDoc}
     */
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }
}
