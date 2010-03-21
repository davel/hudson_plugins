package com.fudeco.hudson.plugins.gerrit;

import com.fudeco.hudson.plugins.gerrit.ssh.SSHMarker;
import com.jcraft.jsch.JSchException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 *
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GerritNotifier extends Notifier {

    private final String git_home;
    private final String gerrit_host;
    private final int gerrit_port;
    private final String gerrit_username;
    
    private final String approve_value;
    private final String reject_value;
    private final String gerrit_approve_command = "gerrit approve --verified=%s --message=%s %s";
    private final String private_key_file_path;
    private final String passPhrase;


    public String getGerrit_host() {
        return gerrit_host;
    }

    public int getGerrit_port() {
        return gerrit_port;
    }

    public String getGerrit_username() {
        return gerrit_username;
    }

    public String getApprove_value() {
        return approve_value;
    }

    public String getReject_value() {
        return reject_value;
    }

    public String getGerrit_approve_command() {
        return gerrit_approve_command;
    }

    public String getPrivate_key_file_path() {
        return private_key_file_path;
    }

    public String getPassPhrase() {
        return passPhrase;
    }


    @SuppressWarnings({"UnusedDeclaration"})
    @DataBoundConstructor
    public GerritNotifier(String git_home, String gerrit_host, int gerrit_port,
            String gerrit_username, String approve_value, String reject_value, String private_key_file_path,
            String passPhrase) {
        this.git_home = git_home;
        this.gerrit_host = gerrit_host;
        this.gerrit_port = gerrit_port;
        this.gerrit_username = gerrit_username;
        this.approve_value = approve_value;
        this.reject_value = reject_value;
        this.private_key_file_path = private_key_file_path;
        this.passPhrase = passPhrase;

    }

    public String getGit_home() {
        return git_home;
    }


    File getGitHome(File workspace) {
        String git_path = workspace.getAbsolutePath() + File.separatorChar + this.git_home;
        File git_home_directory = new File(git_path);
        if (!git_home_directory.isDirectory()) {
            return null;
        }
        return git_home_directory;
    }

    private Repository getRepository(File git_home) {

        Repository repo;
        try {
            repo = new Repository(git_home);
        } catch (IOException e) {
            return null;
        }
        return repo;
    }

    private ObjectId getHead(Repository repo) {
        ObjectId head;
        try {
            head = repo.resolve("HEAD");
        } catch (IOException e) {
            return null;
        }

        return head;
    }

    private void verifyGerrit(String verify_value, String message, String revision)
            throws IOException {

        File privateKeyFile = new File(private_key_file_path);
        SSHMarker marker = new SSHMarker();
        marker.connect(gerrit_host, gerrit_port);
        marker.authenticate(gerrit_username, privateKeyFile, passPhrase);
        String command = String.format(gerrit_approve_command, verify_value, message, revision);
        marker.executeCommand(command);
        marker.disconnect();
    }

    @Override
    public boolean perform(final AbstractBuild build, Launcher launcher, final BuildListener listener)
            throws IOException, InterruptedException {

        FilePath ws = build.getWorkspace();
        
        return ws.act(new FileCallable<Boolean>() {
            // if 'file' is on a different node, this FileCallable will
            // be transfered to that node and executed there.

            public Boolean invoke(File workspace, VirtualChannel channel) {
                // f and file represents the same thing
                File git_home_directory = getGitHome(workspace);
                if (git_home_directory == null) {
                    listener.getLogger().println("Failed to find GIT_HOME in "
                            + workspace.getAbsolutePath() + File.separatorChar + GerritNotifier.this.git_home);

                    build.setResult(Result.ABORTED);
                    return false;
                }
                Repository repo = getRepository(git_home_directory);
                if (repo == null) {
                    listener.getLogger().println("Failed to read repository from "
                            + git_home_directory.getAbsolutePath());
                    build.setResult(Result.ABORTED);
                    return false;
                }
                ObjectId head = getHead(repo);
                if (head == null) {
                    listener.getLogger().println("HEAD is null for " + repo.getDirectory().getAbsolutePath()
                            + ", are you sure that you're using git?");
                    build.setResult(Result.ABORTED);
                    return null;
                }

                try {
                    Result r = build.getResult();
                    EnvVars vars = null;
                    try {
                        vars = build.getEnvironment(listener);
                    } catch (InterruptedException e) {
                        listener.getLogger().println(e.getMessage());
                        e.printStackTrace();
                    }
                    String buildUrl = "No build url.";
                    if (vars.containsKey("BUILD_URL")) {
                        buildUrl = vars.get("BUILD_URL");
                    }
                    if (r.isBetterOrEqualTo(Result.SUCCESS)) {
                        listener.getLogger().println("Approving " + head.name());
                        verifyGerrit(approve_value, buildUrl, head.name());
                    } else if (r.isBetterOrEqualTo(Result.UNSTABLE)) {
                        listener.getLogger().println("Rejecting unstable " + head.name());
                        verifyGerrit(reject_value, "Build is unstable " + buildUrl, head.name());
                    } else {
                        listener.getLogger().println("Rejecting failed " + head.name());
                        verifyGerrit(reject_value, "Build failed " + buildUrl, head.name());
                    }

                } catch (IOException e) {
                    listener.getLogger().println(e.getMessage());
                    e.printStackTrace(listener.getLogger());
                    build.setResult(Result.ABORTED);
                    return false;
                }

                return true;
            }
        });

    }

    // overrided for better type safety.
    // if your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Descriptor for {@link GerritNotifier}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/GerritNotifier/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


        String path_to_private_key_file;
        
        public FormValidation doCheckGerrit_username(@QueryParameter String value)  {
            if (value.length() == 0) {
                return FormValidation.error("Please set a name");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckGerrit_host(@QueryParameter String value)  {
            if (value.length() == 0) {
                return FormValidation.error("Please set a gerritHost");
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckPrivate_key_file_path(@QueryParameter String value)  {
            if (value.length() == 0) {
                return FormValidation.error("Please set a path to private key file");
            }
            File f = new File(value);
            if(!f.exists())
            {
                return FormValidation.error("File doesn't exists");
            }
            if (!SSHMarker.IsPrivateKeyFileValid(f))
            {
                return FormValidation.error("Private key file is not valid");
            }
            path_to_private_key_file = value;

            return FormValidation.ok();
        }

        public FormValidation doCheckPassPhrase(@QueryParameter String value) {

            if(path_to_private_key_file == null) {
                return FormValidation.error("Define path to private key file first");
            }
            File f = new File(path_to_private_key_file);
            if(!f.exists())
            {
                return FormValidation.error("No private key file");
            }
            if (!SSHMarker.IsPrivateKeyFileValid(f))
            {
                return FormValidation.error("Private key file is not valid");
            }
            if (!SSHMarker.CheckPassPhrase(f, value))
            {
                return FormValidation.error("Passphrase is not valid");
            }
            return FormValidation.ok();
        }



        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Gerrit Integration";
        }

        public String guessSSHKeyFile() {
            String user_home = System.getProperty("user.home");
            String ssh_path = user_home + File.separatorChar + ".ssh" + File.separatorChar;

            File f = new File(ssh_path + "id_dsa");
            if(f.exists()) {
                return ssh_path + "id_dsa";
            }
            f = new File(ssh_path + "id_rsa");
            if(f.exists()) {
                return ssh_path + "id_rsa";
            }
            return "";
        }
    }
}

