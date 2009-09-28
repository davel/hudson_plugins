package hudson.plugins.javanet;

import hudson.Extension;
import hudson.triggers.Trigger;
import hudson.triggers.SafeTimerTask;
import hudson.model.listeners.ItemListener;
import hudson.model.AbstractProject;
import hudson.model.Hudson;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
*/
@Extension
public class ItemListenerImpl extends ItemListener {
    public void onLoaded() {
        // when we are installed for the first time, hook this up to all existing jobs
        // so that this can be seen w/o reconfiguration.
        for(AbstractProject<?,?> j : Hudson.getInstance().getAllItems(AbstractProject.class)) {
            StatsProperty p = j.getProperty(StatsProperty.class);
            if(p==null)
                try {
                    j.addProperty(new StatsProperty());
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
        }, debug ? 15*SEC : 10*MINUTE, debug? 15*SEC:3*HOUR);
    }

    static final long SEC = 1000;
    static final long MINUTE = 60*SEC;
    static final long HOUR   = 60*MINUTE;
    static final long DAY   = 24*HOUR;

    /**
     * Debug flag. This will increase the frequency of timer-related work.
     */
    static final boolean debug = Boolean.getBoolean(ItemListenerImpl.class.getName()+".debug");

    private static final Logger LOGGER = Logger.getLogger(ItemListenerImpl.class.getName());
}
