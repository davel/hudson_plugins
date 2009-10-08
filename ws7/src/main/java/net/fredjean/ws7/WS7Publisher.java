/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.fredjean.ws7;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author fjean
 */
public class WS7Publisher extends Notifier {

    private String user;
    private String pwdLocation;
    private String adminHost;
    private String adminPort;
    private String virtualServer;
    private String wsConfig;
    private String warFile;
    private String appContext;
    private String urlToApplication;
    private static final Object lock = new Object();

    /**
     * @stapler-constructor
     */
    public WS7Publisher() {

    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        String cmd = DESCRIPTOR.ws7Location + "/bin/wadm";
        int rc = -1;

        if (validateArguments()) {
            synchronized (lock) {
                /*
                 * Serialize adding the web app to a configuration and deploying
                 * the configuration to avoid issues where multiple projects 
                 * are built around the same time and try to deploy at the same 
                 * time.
                 */

                ArgumentListBuilder args = buildConnectOptions(cmd, "add-webapp");
                args.add("--echo", "--no-prompt", "--config=" + getWsConfig(),
                        "--vs=" + getVirtualServer(), "--uri=" + getAppContext(),
                        getWarFile());

                try {
                    /*
                     * Adds the web application to the server.  
                     */
                    rc = launcher.launch().cmds(args).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getModuleRoot()).join();

                    /*
                     * Redeploy the configuration if the webapp was added succesfully.
                     */
                    if (rc == 0) {
                        args = buildConnectOptions(cmd, "deploy-config");
                        args.add("--echo", "--no-prompt", getWsConfig());

                        rc = launcher.launch().cmds(args).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getModuleRoot()).join();
                    }
                } catch (IOException ioe) {
                    Util.displayIOException(ioe, listener);
                    ioe.printStackTrace();
                    rc = -1;
                }
            }
        }

        return rc == 0;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new WS7ProjectAction(project, this);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * Verifies that all the required arguments are present before we can 
     * launch the command.
     * @return
     */
    private boolean validateArguments() {
        return !isEmpty(getWsConfig()) && !isEmpty(getVirtualServer()) && !isEmpty(getVirtualServer()) && !isEmpty(getAppContext()) && !isEmpty(getWarFile()) && !isEmpty(getUser()) && !isEmpty(getPwdLocation());
    }

    private ArgumentListBuilder buildConnectOptions(String cmd, String wadmcmd) {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(cmd, wadmcmd, "--user=" + getUser(), "--password-file=" + getPwdLocation());
        if (!isEmpty(getAdminHost())) {
            args.add("--host=" + getAdminHost());
        }
        if (!isEmpty(getAdminPort())) {
            args.add("--port=" + getAdminPort());
        }
        return args;
    }
    /**
     * Reference to the Descriptor
     */
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public String getUser() {
        return isEmpty(user) ? DESCRIPTOR.defaultUser : user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwdLocation() {
        return isEmpty(pwdLocation) ? DESCRIPTOR.defaultPwdLocation : pwdLocation;
    }

    public void setPwdLocation(String pwdLocation) {
        this.pwdLocation = pwdLocation;
    }

    public String getAdminHost() {
        return isEmpty(adminHost) ? DESCRIPTOR.defaultAdminHost : adminHost;
    }

    public void setAdminHost(String adminHost) {
        this.adminHost = adminHost;
    }

    public String getAdminPort() {
        return isEmpty(adminPort) ? DESCRIPTOR.defaultAdminPort : adminPort;
    }

    public void setAdminPort(String adminPort) {
        this.adminPort = adminPort;
    }

    public String getVirtualServer() {
        return isEmpty(virtualServer) ? DESCRIPTOR.defaultVS : virtualServer;
    }

    public void setVirtualServer(String virtualServer) {
        this.virtualServer = virtualServer;
    }

    public String getWsConfig() {
        return isEmpty(wsConfig) ? DESCRIPTOR.defaultConfig : wsConfig;
    }

    public void setWsConfig(String wsConfig) {
        this.wsConfig = wsConfig;
    }

    public String getWarFile() {
        return warFile;
    }

    public void setWarFile(String warFile) {
        this.warFile = warFile;
    }

    public String getUrlToApplication() {
        return urlToApplication;
    }

    public void setUrlToApplication(String urlToApplication) {
        this.urlToApplication = urlToApplication;
    }

    public String getAppContext() {
        return appContext;
    }

    public void setAppContext(String appContext) {
        this.appContext = appContext;
    }

    private boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String ws7Location = "/sun/webserver7";
        private String defaultUser = "admin";
        private String defaultPwdLocation;
        private String defaultAdminHost;
        private String defaultAdminPort;
        private String defaultConfig;
        private String defaultVS;

        public DescriptorImpl() {
            super(WS7Publisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Deploy to SJS Web Server 7";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            ws7Location = req.getParameter("ws7.ws7Location");
            defaultUser = req.getParameter("ws7.defaultUser");
            defaultPwdLocation = req.getParameter("ws7.defaultPwdLocation");
            defaultAdminHost = req.getParameter("ws7.defaultAdminHost");
            defaultAdminPort = req.getParameter("ws7.defaultAdminPort");
            defaultConfig = req.getParameter("ws7.defaultConfig");
            defaultVS = req.getParameter("ws7.defaultVS");
            save();
            return super.configure(req, formData);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            WS7Publisher ws7 = new WS7Publisher();
            req.bindParameters(ws7, "ws7.");
            return ws7;
        }

        public String getWs7Location() {
            return ws7Location;
        }

        public void setWs7Location(String ws7Location) {
            this.ws7Location = ws7Location;
        }

        public String getDefaultUser() {
            return defaultUser;
        }

        public void setDefaultUser(String defaultUser) {
            this.defaultUser = defaultUser;
        }

        public String getDefaultPwdLocation() {
            return defaultPwdLocation;
        }

        public void setDefaultPwdLocation(String defaultPwdLocation) {
            this.defaultPwdLocation = defaultPwdLocation;
        }

        public String getDefaultAdminHost() {
            return defaultAdminHost;
        }

        public void setDefaultAdminHost(String defaultAdminHost) {
            this.defaultAdminHost = defaultAdminHost;
        }

        public String getDefaultAdminPort() {
            return defaultAdminPort;
        }

        public void setDefaultAdminPort(String defaultAdminPort) {
            this.defaultAdminPort = defaultAdminPort;
        }

        public String getDefaultVS() {
            return defaultVS;
        }

        public void setDefaultVS(String defaultVS) {
            this.defaultVS = defaultVS;
        }

        public String getDefaultConfig() {
            return defaultConfig;
        }

        public void setDefaultConfig(String defaultConfig) {
            this.defaultConfig = defaultConfig;
        }
    }
}
