package hudson.plugins.javanet_trigger_installer;

import hudson.model.Project;
import hudson.model.AbstractProject;
import hudson.scm.CVSSCM;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;
import hudson.triggers.Trigger;
import org.kohsuke.jnt.JNMailingList;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;
import org.kohsuke.jnt.SubscriptionMode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the subscription/unsubscription related work to be executed.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class Task {

    final AbstractProject<?,?> project;


    public Task(AbstractProject project) {
        this.project = project;
    }

    /**
     * Updates the project's configuration from the actual java.net setting.
     */
    static final class Check extends Task {
        public Check(Project project) {
            super(project);
        }

        protected void execute(JNMailingList list) throws ProcessingException, IOException {
            if(getSubscriptionAddress(list)!=null) {
                if(project.getTrigger(JavaNetScmTrigger.class)!=null)
                    return;
                LOGGER.info(project.getName()+" is apparently hooked to the java.net SCM change trigger");
                project.addTrigger(new JavaNetScmTrigger());
            } else {
                if(project.getTrigger(JavaNetScmTrigger.class)==null)
                    return;
                LOGGER.info(project.getName()+" is apparently removed from the java.net SCM change trigger");
                project.removeTrigger(Trigger.all().get(JavaNetScmTrigger.DescriptorImpl.class));
            }
        }
    }

    /**
     * Updates java.net setting from the project configuration
     */
    static final class Update extends Task {
        public Update(AbstractProject project) {
            super(project);
        }

        protected void execute(JNMailingList list) throws ProcessingException, IOException {
            // shall we subscribe or unsubscribe?
            boolean subscribe = project.getTrigger(JavaNetScmTrigger.class)!=null;

            String adrs = getSubscriptionAddress(list);
            if(adrs !=null) {
                if(subscribe)
                    return; // already subscribed
                LOGGER.info("Unsubscribing "+project.getName()+" from java.net SCM trigger");
                list.massUnsubscribe(Collections.singletonList(adrs), SubscriptionMode.NORMAL, null);
            } else {
                if(!subscribe)
                    return; // already unsubscribed
                LOGGER.info("Subscribing "+project.getName()+" to java.net SCM trigger");
                list.massSubscribe("hudson-"+escape(project.getName())+calcSuffix()+"@hudson.sfbay.sun.com",
                    SubscriptionMode.NORMAL);
            }
        }

        private String calcSuffix() {
            SCM scm = project.getScm();
            if(scm instanceof CVSSCM) {
                String branch = ((CVSSCM)scm).getBranch();
                if(branch!=null)
                    return "+branch="+branch;
                else
                    return "+branch=trunk";
            }

            // no particular suffix
            return "";
        }
    }

    /**
     * Schedules the execution of this task.
     * It will be executed at some later point.
     */
    void schedule() {
        synchronized(Worker.queue) {
            Worker.queue.add(this);
            Worker.queue.notify();
        }
    }

    /**
     * Schedules the execution of this task
     * for at some later point, but make sure
     * it won't be blocked by other lower priority background tasks.
     */
    void scheduleHighPriority() {
        synchronized(Worker.queue) {
            Worker.queue.add(0,this);
            Worker.queue.notify();
        }
    }

    /**
     * Figures out the java.net project for this project.
     *
     * @return
     *      null if we couldn't find it.
     */
    private JNProject getProject(JavaNet con) throws ProcessingException {
        SCM scm = project.getScm();
        if (scm instanceof CVSSCM) {
            CVSSCM cvs = (CVSSCM) scm;
            String cvsroot = cvs.getCvsRoot();

            // basic sanity checking
            // we can't test 'cvs.dev.java.net' because of the bridges
            if(!cvsroot.endsWith(":/cvs"))
                return null;

            // then check the module name
            String m = cvs.getAllModules();
            if(m.indexOf(" ")>=0)
                return null;    // no support for multi-module for now.

            int idx = m.indexOf('/');
            if(idx>=0)
                m = m.substring(0,idx);

            return con.getProject(m);
        }

        if (scm instanceof SubversionSCM) {
            SubversionSCM svn = (SubversionSCM)scm;
            ModuleLocation[] locs = svn.getLocations();

            if(locs.length==0)
                return null;    // no support for multi-module for now.

            try {
                URL url = new URL(locs[0].remote);

                Matcher matcher = SVN_PATH_PATTERN.matcher(url.getPath());
                if(!matcher.matches())
                    return null;    // doesn't look like java.net URL
                return con.getProject(matcher.group(1));
            } catch (MalformedURLException e) {
                // this shouldn't really happen
                LOGGER.info("Failed to parse SVN URL "+locs[0].remote);
                return null;
            }

        }

        // java.net only support CVS and SVN
        return null;
    }

    public void execute(JavaNet connection) throws IOException , ProcessingException {
        JNProject p = getProject(connection);
        if(p==null)
            return;

        if(project.getScm() instanceof CVSSCM) {
            execute(p.getMailingLists().get("cvs"));
            return;
        }

        if(project.getScm() instanceof SubversionSCM) {
            execute(p.getMailingLists().get("commits"));
            return;
        }

        // huh?
    }

    /**
     * Checks if the trigger e-mail address is already subscribed.
     */
    protected final String getSubscriptionAddress(JNMailingList list) throws ProcessingException {
        Pattern triggerAddress = Pattern.compile("hudson-"+escape(project.getName())+"(\\+.+)?@(kohsuke|hudson)\\.sfbay\\.sun\\.com");

        for (String adrs : list.getSubscribers(SubscriptionMode.NORMAL)) {
            if(triggerAddress.matcher(adrs).matches())
                return adrs;
        }
        return null;
    }

    /**
     * Escapes the character unsafe for e-mail address.
     */
    private static String escape(String projectName) {
        // TODO: escape non-ASCII characters
        StringBuilder buf = new StringBuilder(projectName.length());
        for( int i=0; i<projectName.length(); i++ ) {
            char ch = projectName.charAt(i);
            if(('a'<=ch && ch<='z')
            || ('z'<=ch && ch<='Z')
            || ('0'<=ch && ch<='9')
            || "-_.".indexOf(ch)>=0)
                buf.append(ch);
            else
                buf.append('_');    // escape
        }
        return projectName;
    }

    /**
     * Acts on the mailing list to perform the task.
     */
    protected abstract void execute(JNMailingList list) throws ProcessingException, IOException;

    private static final Logger LOGGER = Logger.getLogger(Task.class.getName());

    private static final Pattern SVN_PATH_PATTERN = Pattern.compile("/svn/([^/]+)/.+");
}
