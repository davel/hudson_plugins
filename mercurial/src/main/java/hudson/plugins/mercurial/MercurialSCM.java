package hudson.plugins.mercurial;

import hudson.*;
import hudson.FilePath.FileCallable;
import hudson.model.*;
import hudson.plugins.mercurial.browser.HgWeb;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Mercurial SCM.
 * 
 * @author Kohsuke Kawaguchi
 */
public class MercurialSCM extends SCM implements Serializable {
    /**
     * Source repository URL from which we pull.
     */
    private final String source;

    /**
     * In-repository branch to follow. Null indicates "default". 
     */
    private final String branch;

    private HgWeb browser;

    @DataBoundConstructor
    public MercurialSCM(String source, String branch, HgWeb browser) {
        this.source = source;

        // normalization
        branch = Util.fixEmpty(branch);
        if(branch!=null && branch.equals("default"))
            branch = null;
        this.branch = branch;

        this.browser = browser;
    }

    /**
     * Gets the source repository path.
     * Either URL or local file path.
     */
    public String getSource() {
        return source;
    }

    /**
     * In-repository branch to follow. Null indicates "default".
     */
    public String getBranch() {
        return branch;
    }

    @Override
    public HgWeb getBrowser() {
        return browser;
    }

    @Override
    public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        String remoteTip = getTipRevision(launcher,workspace,listener);
        PrintStream output = listener.getLogger();

        if(launcher.launch(
            new String[]{getDescriptor().getHgExe(),"id","-r",remoteTip},
            EnvVars.masterEnvVars, output,workspace).join()==0) {

            output.println("No changes");
            return false;
        }

        output.println("Changes detected");
        return true;
    }

    /**
     * Determines the current tip revision id in the upstream and return it.
     */
    private String getTipRevision(Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getDescriptor().getHgExe(),"id");
        if(branch!=null)
            args.add("-r",branch);
        args.add("default");

        if(launcher.launch(args.toCommandArray(),EnvVars.masterEnvVars,baos,workspace).join()!=0) {
            // dump the output from hg to assist trouble-shooting.
            Util.copyStream(new ByteArrayInputStream(baos.toByteArray()),listener.getLogger());
            listener.error("Failed to check the tip revision");
            throw new AbortException();
        }

        // obtain the current changeset node number
        return new String(baos.toByteArray(), "ASCII").trim();
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        boolean canUpdate = workspace.act(new FileCallable<Boolean>() {
            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                if(!HgRc.getHgRcFile(ws).exists())
                    return false;
                HgRc hgrc = new HgRc(ws);
                return canUpdate(hgrc);
            }

            private boolean canUpdate(HgRc ini) {
                String upstream = ini.getSection("paths").get("default");
                if(upstream==null)  return false;

                if(upstream.equals(source)) return true;
                return source.startsWith("file:/") && new File(upstream).toURI().toString().equals(source);
            }
        });

        if(canUpdate)
            return update(build,launcher,workspace,listener,changelogFile);
        else
            return clone(build,launcher,workspace,listener,changelogFile);
    }

    /**
     * Updates the current workspace.
     */
    private boolean update(AbstractBuild<?,?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException, IOException {
        FilePath hgBundle = new FilePath(workspace, "hg.bundle");

        // delete the file prior to "hg incoming",
        // as one user reported that it causes a failure.
        // The error message was "abort: file 'hg.bundle' already exists"
        hgBundle.delete();

        // calc changelog and create bundle
        FileOutputStream os = new FileOutputStream(changelogFile);
        os.write("<changesets>\n".getBytes());
        int r;
        try {
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(getDescriptor().getHgExe(),"incoming","--quiet","--bundle","hg.bundle");
            args.add("--template", MercurialChangeSet.CHANGELOG_TEMPLATE);

            // NOTE:
            // Need to pass debug flag to get list of adds and deletes for changeset view
            // 		http://marc.info/?l=mercurial&m=116595041401081&w=2
            // 		http://www.selenic.com/pipermail/mercurial/2007-September/014481.html
            // or maybe using a style file would do it?
            //		http://mail-archives.apache.org/mod_mbox/maven-issues/200610.mbox/%3C76668191.1160089827292.JavaMail.haus-jira@codehaus01.managed.contegix.com%3E
            // 
            // TODO: Noted in the above links this may be expensive to compute, consider 
            //       making it an option; should run some cursory test to determine how expensive.
            // TODO: This is also spammy in the changelog.  Should file a feature request to fix this as described in the first link.
            args.add("--debug");

            if(branch!=null)    args.add("-r",branch);

            ByteArrayOutputStream errorLog = new ByteArrayOutputStream();

            // mercurial produces text in the platform default encoding, so we need to
            // convert it back to UTF-8
            WriterOutputStream o = new WriterOutputStream(new OutputStreamWriter(os, "UTF-8"));
            try {
                r = launcher.launch(args.toCommandArray(),build.getEnvVars(), new ForkOutputStream(o,errorLog), workspace).join();
            } finally {
                o.flush(); // make sure to commit all output
            }
            if(r!=0 && r!=1) {// 0.9.4 returns 1 for no changes
                Util.copyStream(new ByteArrayInputStream(errorLog.toByteArray()),listener.getLogger());
                listener.error("Failed to determine incoming changes");
                return false;
            }
        } catch (IOException e) {
            listener.error("Failed to pull");
            return false;
        } finally {
            os.write("</changesets>".getBytes());
            os.close();
        }

        // pull
        if(r==0 && hgBundle.exists())
            // if incoming didn't fetch anything, it will return 1. That was for 0.9.3.
            // in 0.9.4 apparently it returns 0.
            try {
                if(launcher.launch(
                    new String[]{getDescriptor().getHgExe(),"pull","-u","hg.bundle"},
                    build.getEnvVars(),listener.getLogger(),workspace).join()!=0) {
                    listener.error("Failed to pull");
                    return false;
                }
            } catch (IOException e) {
                listener.error("Failed to pull");
                return false;
            }

        return true;
    }


    /**
     * Start from scratch and clone the whole repository.
     */
    private boolean clone(AbstractBuild<?,?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException {
        try {
            workspace.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to clean the workspace"));
            return false;
        }

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getDescriptor().getHgExe(),"clone");
        if(branch!=null)    args.add("-r",branch);
        args.add(source,workspace.getRemote());
        try {
            if(launcher.launch(args.toCommandArray(),build.getEnvVars(),listener.getLogger(),null).join()!=0) {
                listener.error("Failed to clone "+source);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to clone "+source));
            return false;
        }

        return createEmptyChangeLog(changelogFile, listener, "changelog");
    }

    @Override
    public void buildEnvVars(AbstractBuild build, Map<String, String> env) {
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new MercurialChangeLogParser();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DescriptorImpl.DESCRIPTOR;
    }

    public static final class DescriptorImpl extends SCMDescriptor<MercurialSCM> {
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

        private String hgExe;

        private  DescriptorImpl() {
            super(MercurialSCM.class, HgWeb.class);
            load();
        }

        public String getDisplayName() {
            return "Mercurial";
        }

        /**
         * Path to mercurial executable.
         */
        public String getHgExe() {
            if(hgExe==null) return "hg";
            return hgExe;
        }

        public SCM newInstance(StaplerRequest req) throws FormException {
            //return req.bindParameters(MercurialSCM.class,"mercurial.");
            return new MercurialSCM(
                    req.getParameter("mercurial.source"),
                    req.getParameter("mercurial.branch"),
                    RepositoryBrowsers.createInstance(HgWeb.class, req, "mercurial.browser"));
        }

        public boolean configure(StaplerRequest req) throws FormException {
            hgExe = req.getParameter("mercurial.hgExe");
            save();
            return true;
        }

        public void doHgExeCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator.Executable(req,rsp) {
                protected void checkExecutable(File exe) throws IOException, ServletException {
                    ByteBuffer baos = new ByteBuffer();
                    try {
                        Proc proc = Hudson.getInstance().createLauncher(TaskListener.NULL).launch(
                                new String[]{getHgExe(), "version"}, new String[0], baos, null);
                        proc.join();

                        Matcher m = VERSION_STRING.matcher(baos.toString());
                        if(m.find()) {
                            try {
                                if(new VersionNumber(m.group(1)).compareTo(V0_9_4)>=0) {
                                    ok(); // right version
                                } else {
                                    error("This hg is ver."+m.group(1)+" but we need 0.9.4");
                                }
                            } catch (IllegalArgumentException e) {
                                warning("Hudson can't tell if this hg is 0.9.4 or later (detected version is %s)",m.group(1));
                            }
                            return;
                        }
                    } catch (IOException e) {
                        // failed
                    } catch (InterruptedException e) {
                        // failed
                    }
                    error("Unable to check hg version");
                }
            }.process();
        }

        /**
         * Pattern matcher for the version number.
         */
        private static final Pattern VERSION_STRING = Pattern.compile("\\(version ([0-9.]+)");

        private static final VersionNumber V0_9_4 = new VersionNumber("0.9.4");
    }


    private static final long serialVersionUID = 1L;
}
