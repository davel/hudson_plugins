package org.jggug.hudson.plugins.gcrawler;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;

import java.io.IOException;

@Extension
public class GBuildWrapper extends BuildWrapper {

    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Environment setUp(final AbstractBuild build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        class EnvironmentImpl extends Environment {

            private TimerTask task;

            private EnvironmentImpl() {
                this.task = new TimerTask(build, listener);
                Trigger.timer.schedule(task, 0, 1000);
            }

            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                task.cancel();
                return !task.isSizeOver;
            }

            final class TimerTask extends SafeTimerTask {

                private boolean isSizeOver;

                private AbstractBuild build;

                private BuildListener listener;

                private TimerTask(AbstractBuild build, BuildListener listener) {
                    this.build = build;
                    this.listener = listener;
                }

                @Override
                protected void doRun() {
                    if (build.getLogFile().length() >= (10 * 1024 * 1024)) {
                        listener.getLogger().append("Build interrupted. Log file is too large..\n");
                        isSizeOver = true;
                        Executor executor = build.getExecutor();
                        if (executor != null) {
                            executor.interrupt();
                        }
                    }
                }
            }
        }

        return new EnvironmentImpl();
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        DescriptorImpl() {
            super(GBuildWrapper.class);
        }

        public String getDisplayName() {
            return "Abort the build if it's stuck";
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }
}
