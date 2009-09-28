package hudson.plugins.javanet;

import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.scm.CVSSCM;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;
import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link JobProperty} so that this plugin can contribute
 * {@link JavaNetStatsAction} to the project, which in turn
 * displays a link in the project page.
 *
 * @author Kohsuke Kawaguchi
 */
public class StatsProperty extends JobProperty<AbstractProject<?,?>> {
    public JavaNetStatsAction getJobAction(AbstractProject<?,?> job) {
        String jnp = getJavaNetProject(job);
        if(jnp==null)
            return null;
        return new JavaNetStatsAction(job,jnp);
    }

    private String getJavaNetProject(AbstractProject<?,?> job) {
        String v = JavaNetStatsAction.readOverrideFile(job);
        if(v!=null)     return v;
        return getJavaNetProject(job.getScm());
    }

    /**
     * Determines if this SCM is a java.net project,
     * and if so, return the project name, otherwise null.
     */
    private String getJavaNetProject(SCM scm) {
        if (scm instanceof SubversionSCM) {
            SubversionSCM sscm = (SubversionSCM) scm;
            for (ModuleLocation loc : sscm.getLocations()) {
                Matcher m = SVN_URL.matcher(loc.remote);
                if(m.matches())
                    return m.group(1);
            }
        }
        if (scm instanceof CVSSCM) {
            CVSSCM cscm = (CVSSCM) scm;
            if(CVS_URL.matcher(cscm.getCvsRoot()).matches()) {
                StringTokenizer tokens = new StringTokenizer(
                    cscm.getAllModules(), // TODO: use getAllModulesNormalized
                    " /\\");
                return tokens.nextToken();
            }
        }
        return null;
    }

    private static final Pattern SVN_URL = Pattern.compile("https://[^.]+.dev.java.net/svn/([^/]+)(/.*)?");

    private static final String USERNAME = "([A-Za-z0-9_\\-])+";
    private static final String HOST = "(.*.dev.java.net|kohsuke.sfbay.*)";
    private static final Pattern CVS_URL = Pattern.compile(
        ":pserver:"+USERNAME+"@"+HOST+":/cvs");

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return null;
        }

        public StatsProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new StatsProperty();
        }
    }
}
