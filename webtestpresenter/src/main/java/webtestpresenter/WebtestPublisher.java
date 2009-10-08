package webtestpresenter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Publishes links to canoo webtest results.
 * 
 * @see http://webtest.canoo.com
 *
 * @author Adam Ambrose
 */
public class WebtestPublisher extends Notifier implements Serializable {
    // Uncomment for debugging:
    //private static final Logger LOG = 
    //    Logger.getLogger(WebtestPublisher.class.getName());
    private static final String WEBTEST_REPORTS = "webtestReports";
	private static final long serialVersionUID = 1L;
    private final String webtestResultsSrcDir;

    WebtestPublisher(String webtestResultsSrcDir) {
        this.webtestResultsSrcDir= webtestResultsSrcDir;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getWebtestResultsSrcDir() {
        return webtestResultsSrcDir;
    }
    /**
     * Gets the directory where the latest Webtest results are stored for the
     * given project.
     */
    private static FilePath getWebtestReportDir(AbstractBuild<?,?> build) {
        //return new File(build.getArtifactsDir(), "webtestReports");
        return new FilePath(
                new File(build.getRootDir(), WEBTEST_REPORTS));
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public boolean perform(AbstractBuild<?,?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException {

        // TODO: does time check need to account for running on slave?
        // see hudson.tasks.junit.JUnitResultsArchiver
        final long buildTime = build.getTimestamp().getTimeInMillis();
        
        FilePath webtestResults =
            build.getWorkspace().child(webtestResultsSrcDir);
        FilePath target = getWebtestReportDir(build);
        Action action = null;

        try {
            if (!webtestResults.exists()) {
                return true;
            }
            // Check if the results are stale.  If not, copy them over
            // anyways.
            if (build.getResult().isWorseOrEqualTo(Result.FAILURE) &&
                    buildTime + 1000 /*error margin*/ >
                    webtestResults.lastModified()) {
                listener.getLogger().println(
                        "Webtest reports are stale.  Not publishing.");
                return true;
            }

            StringBuffer msgBuf = new StringBuffer("Publishing webtest results: ");
            msgBuf.append(webtestResultsSrcDir);
            msgBuf.append(" to: ");
            msgBuf.append(target);
            listener.getLogger().println(msgBuf.toString());
            webtestResults.copyRecursiveTo("**/*", target);
            action = new WebtestReportAction(build);
        } catch (IOException e) {
            Util.displayIOException(e,listener);
            String err = "Unable to copy Webtest results from "
                + webtestResults + " to " + target;
            e.printStackTrace(listener.fatalError(err));
            build.setResult(Result.FAILURE);
        }

        build.getActions().add(action);
        return true;
    }

    public Action getProjectAction(AbstractBuild<?,?> build) {
        return new WebtestReportAction(build);
    }

    /**
     * Descriptor for {@link WebtestPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        public static final String CONFIG_PARAM =
            "webtest_publisher.webtestResultsSrc";
        private String webtestResultsSrc;

        public DescriptorImpl() {
            super(WebtestPublisher.class);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish Webtest results";
        }

        public String getWebtestResultsSrc() {
            return webtestResultsSrc;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            webtestResultsSrc = req.getParameter(CONFIG_PARAM);
            save();
            return super.configure(req, formData);
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public FormValidation doCheck(@AncestorInPath AbstractProject project,
                                      @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Creates a new instance of {@link WebtestPublisher} from a submitted
         * form.
         */
        @Override
        public WebtestPublisher newInstance(StaplerRequest req, JSONObject formData)
            throws FormException {
            // see config.jelly and you'll find "hello_world.name" form entry.
            return new WebtestPublisher(req.getParameter(CONFIG_PARAM));
        }
    }

    public static final class WebtestReportAction implements Action {
        private static final long serialVersionUID = -3578312514160132918L;
        /**
         * Project that owns this action.
         */
        public final AbstractBuild<?,?> build;

        public WebtestReportAction(AbstractBuild<?,?> build) {
            this.build = build;
        }

        public String getUrlName() {
            return "webtestResults";
        }

        public String getDisplayName() {
            return "Webtest Results";
        }

        public String getIconFileName() {
            return "clipboard.gif";
        }

        public Object getTarget() {
            if (build != null) {
                return getWebtestReportDir(build);
            }
            return null;
        }

        public DirectoryBrowserSupport doDynamic(StaplerRequest req, StaplerResponse rsp) {
            if(this.build != null) {
                return new DirectoryBrowserSupport(this, getWebtestReportDir(this.build),
                                                   "webtest", "clipboard.gif", false);
            }
            return null;
        }
    }
}
