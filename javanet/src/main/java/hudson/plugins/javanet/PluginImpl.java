package hudson.plugins.javanet;

import hudson.Plugin;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Jobs;
import hudson.model.listeners.ItemListener;
import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    public void start() {
        Jobs.PROPERTIES.add(StatsProperty.DESCRIPTOR);

        Hudson.getInstance().getJobListeners().add(new ItemListener() {
            public void onLoaded() {
                // when we are installed for the first time, hook this up to all existing jobs
                // so that this can be seen w/o reconfiguration.
                for(AbstractProject<?,?> j : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                    StatsProperty p = j.getProperty(StatsProperty.class);
                    if(p==null)
                        try {
                            ((AbstractProject)j).addProperty(new StatsProperty());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to persist "+j,e);
                        }
                }

                Trigger.timer.scheduleAtFixedRate(new SafeTimerTask() {
                    protected void doRun() {
                        LOGGER.fine("Starting up-to-date check of java.net stat reports");
                        long startTime = System.currentTimeMillis();
                        try {
                            for(AbstractProject<?,?> j : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                                StatsProperty p = j.getProperty(StatsProperty.class);
                                if(p==null) continue;
                                JavaNetStatsAction a = p.getJobAction(j);
                                if(a==null)  continue;

                                a.upToDateCheck();
                            }
                        } finally {
                            LOGGER.fine("Completing up-to-date check of java.net stat reports. Took "+(System.currentTimeMillis()-startTime)+"ms");
                        }
                    }
                },debug?15*SEC:10*MINUTE,debug?15*SEC:3*HOUR);
            }
        });
    }

    static final long SEC = 1000;
    static final long MINUTE = 60*SEC;
    static final long HOUR   = 60*MINUTE;
    static final long DAY   = 24*HOUR;

    private static final Logger LOGGER = Logger.getLogger(PluginImpl.class.getName());

    /**
     * Debug flag. This will increase the frequency of timer-related work.
     */
    static final boolean debug = Boolean.getBoolean(PluginImpl.class.getName()+".debug");
}
