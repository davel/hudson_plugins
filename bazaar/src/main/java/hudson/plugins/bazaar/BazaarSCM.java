package hudson.plugins.bazaar;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath.FileCallable;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.io.ByteBuffer;

/**
 * Bazaar SCM.
 * 
 * @author Trond Norbye
 */
public class BazaarSCM extends SCM implements Serializable {

    /**
     * Source repository URL from which we pull.
     */
    private final String source;
    private final boolean clean;

    @DataBoundConstructor
    public BazaarSCM(String source, boolean clean) {
        this.source = source;
        this.clean = clean;
    }

    /**
     * Gets the source repository path.
     * Either URL or local file path.
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * True if we want clean check out each time. This means deleting everything in the workspace
     * @return
     */
    public boolean isClean() {
        return clean;
    }

    private String getRevid(Launcher launcher, FilePath workspace, String root) throws InterruptedException {
        String rev = null;
        try {
            int ret;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if ((ret = launcher.launch().cmds(getDescriptor().getBzrExe(), "revision-info", "-d", root)
                    .envs(EnvVars.masterEnvVars).stdout(baos).pwd(workspace).join()) != 0) {
                logger.warning("bzr revision-info -d " + root + " returned " + ret + ". Command output: \"" + baos.toString() + "\"");
            } else {
              String[] infos = baos.toString().split("\\s");
              rev = infos[1];
            }
        } catch (IOException e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            logger.log(Level.WARNING, "Failed to poll repository: ", e);
        }

        if (rev == null) {
            logger.warning("Failed to get revision id for: " + workspace);
        }

        return rev;
    }

    private void getLog(Launcher launcher, FilePath workspace, String oldver, String newver, File changeLog) throws InterruptedException {
        try {
            int ret;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String version = "revid:" + oldver + "..revid:" + newver;
            if ((ret = launcher.launch().cmds(getDescriptor().getBzrExe(), "log", "-v", "-r", version, "--long", "--show-ids")
                    .envs(EnvVars.masterEnvVars).stdout(baos).pwd(workspace).join()) != 0) {
                logger.warning("bzr log -v -r returned " + ret);
            } else {
                FileOutputStream fos = new FileOutputStream(changeLog);
                fos.write(baos.toByteArray());
                fos.close();
            }
        } catch (IOException e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            logger.log(Level.WARNING, "Failed to poll repository: ", e);
        }
    }

    @Override
    public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {

        PrintStream output = listener.getLogger();

        output.println("Getting upstream revision...");
        String upstream = getRevid(launcher, workspace, source);
        output.println(upstream);

        output.println("Getting local revision...");
        String local = getRevid(launcher, workspace, workspace.getRemote());
        output.println(local);

        return ! upstream.equals(local);
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        boolean canUpdate = workspace.act(new FileCallable<Boolean>() {

            private static final long serialVersionUID = 1L;

            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                File file = new File(ws, ".bzr");
                return file.exists();
            }
        });

        if (canUpdate && !clean) {
            return update(build, launcher, workspace, listener, changelogFile);
        } else {
            return clone(build, launcher, workspace, listener, changelogFile);
        }
    }

    /**
     * Updates the current workspace.
     */
    private boolean update(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException, IOException {
        try {
            String oldid = getRevid(launcher, workspace, workspace.getRemote());

            if (launcher.launch().cmds(getDescriptor().getBzrExe(), "pull", "--overwrite", source)
                    .envs(build.getEnvironment(listener)).stdout(listener.getLogger()).pwd(workspace).join() != 0) {
                listener.error("Failed to pull");
                return false;
            }

            String newid = getRevid(launcher, workspace, workspace.getRemote());
            getLog(launcher, workspace, oldid, newid, changelogFile);

        } catch (IOException e) {
            listener.error("Failed to pull");
            return false;
        }

        return true;
    }

    /**
     * Start from scratch and clone the whole repository.
     */
    private boolean clone(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException {
        try {
            workspace.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to clean the workspace"));
            return false;
        }

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getDescriptor().getBzrExe(), "branch");
        args.add(source, workspace.getRemote());
        try {
            if (launcher.launch().cmds(args).envs(build.getEnvironment(listener)).stdout(listener.getLogger()).join() != 0) {
                listener.error("Failed to clone " + source);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to clone " + source));
            return false;
        }

        return createEmptyChangeLog(changelogFile, listener, "changelog");
    }

    @Override
    public void buildEnvVars(AbstractBuild build, Map<String, String> env) {
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new BazaarChangeLogParser();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DescriptorImpl.DESCRIPTOR;
    }

    public static final class DescriptorImpl extends SCMDescriptor<BazaarSCM> {
        @Extension
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
        private String bzrExe;
        private transient String version;

        private DescriptorImpl() {
            super(BazaarSCM.class, null);
            load();
        }

        public String getDisplayName() {
            return "Bazaar";
        }

        /**
         * Path to bazaar executable.
         * @return
         */
        public String getBzrExe() {
            return (bzrExe == null) ? "bzr" : bzrExe;
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            BazaarSCM scm = req.bindJSON(BazaarSCM.class, formData);
            return scm;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            bzrExe = req.getParameter("bazaar.bzrExe");
            version = null;
            save();
            return true;
        }

        public FormValidation doBzrExeCheck(@QueryParameter final String value) throws IOException, ServletException {
            return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {
                @Override public FormValidation validate(File exe) {
                    try {
                       ByteBuffer baos = new ByteBuffer();
                       if (Hudson.getInstance().createLauncher(TaskListener.NULL).launch()
                               .cmds(getBzrExe(), "--version").stdout(baos).join() == 0) {
                          return FormValidation.ok();
                       } else {
                          return FormValidation.warning("Could not locate the executable in path");
                       }
                    } catch (IOException e) {
                        // failed
                    } catch (InterruptedException e) {
                        // failed
                    }
                    return FormValidation.error("Unable to check bazaar version");
                }
            });
        }

        /**
         * UUID version string.
         * This appears to be used for snapshot builds. See issue #1683
         */
        private static final Pattern UUID_VERSION_STRING = Pattern.compile("\\(version ([0-9a-f]+)");
    }
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(BazaarSCM.class.getName());
}
