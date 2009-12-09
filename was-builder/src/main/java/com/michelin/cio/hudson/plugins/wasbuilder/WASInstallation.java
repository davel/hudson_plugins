/*
 * The MIT License
 *
 * Copyright (c) 2009, Manufacture Française des Pneumatiques Michelin, Romain Seguy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.michelin.cio.hudson.plugins.wasbuilder;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Corresponds to an IBM WebSphere Application Server installation (currently,
 * it has been tested with WAS 6.0 and WAS 7.0)
 *
 * <p>To use a {@link WASBuildStep} build step, it is mandatory to define an
 * installation: No default installations can be assumed as we necessarily need
 * {@code wsadmin.bat}/{@code wsadmin.sh}.</p>
 *
 * @author Romain Seguy
 * @version 1.0
 */
public class WASInstallation extends ToolInstallation implements NodeSpecific<WASInstallation>, EnvironmentSpecific<WASInstallation> {

    public final static String WSADMIN_BAT = "wsadmin.bat";
    public final static String WSADMIN_SH = "wsadmin.sh";

    @DataBoundConstructor
    public WASInstallation(String name, String home) {
        super(name, removeTrailingBackslash(home), Collections.EMPTY_LIST);
    }

    public WASInstallation forEnvironment(EnvVars env) {
        return new WASInstallation(getName(), env.expand(getHome()));
    }

    public WASInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new WASInstallation(getName(), translateFor(node, log));
    }

    public static WASInstallation getWasInstallationByName(String installationName) {
        for(WASInstallation installation: Hudson.getInstance().getDescriptorByType(WASInstallation.DescriptorImpl.class).getInstallations()) {
            if(installationName != null && installation.getName().equals(installationName)) {
                return installation;
            }
        }

        return null;
    }

    public String getWsadminExecutable(Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new Callable<String,IOException>() {
            public String call() throws IOException {
                // is wsadmin.bat/wsadmin.sh there?
                File wsadminFile = getWsadminFile("bin");
                if(wsadminFile.exists()) {
                    return wsadminFile.getPath();
                }
                return null;
            }
        });
    }

    /**
     * Returns a {@link File} representing {@code wsadmin.bat}/{@code wsadmin.sh}.
     */
    private File getWsadminFile(String binFolder) {
        String wsadminFileName = WSADMIN_SH;

        if(Hudson.isWindows()) {
            wsadminFileName = WSADMIN_BAT;
        }

        return new File(Util.replaceMacro(getHome(), EnvVars.masterEnvVars), binFolder + "/" + wsadminFileName);
    }

    /**
     * Removes the '\' or '/' character that may be present at the end of the
     * specified string.
     */
    private static String removeTrailingBackslash(String s) {
        return StringUtils.removeEnd(StringUtils.removeEnd(s, "/"), "\\");
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<WASInstallation> {

        private WASServer[] servers;

        public DescriptorImpl() {
            // let's avoid a NullPointerException in getInstallations()
            setInstallations(new WASInstallation[0]);

            load();
        }

        /**
         * Returns the possible connection types to WAS.
         *
         * <p>This method needs to be placed here so that the list can be
         * accessible from WASInstallation's global.jelly file: global.jelly
         * is not able to access such a method if it is placed, even statically,
         * into WASServer.</p>
         */
        public String[] getConntypes() {
            return WASServer.CONNTYPES;
        }

        @Override
        public String getDisplayName() {
            return ResourceBundleHolder.get(WASBuildStep.class).format("DisplayName");
        }

        public WASServer[] getServers() {
            if(servers != null) {
                return servers.clone();
            }

            return null;
        }

        private void setServers(WASServer... servers) {
            if(servers != null) {
                this.servers = servers.clone();
            }
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            setInstallations(
                    req.bindJSONToList(
                            WASInstallation.class,
                            formData.get("wasinstall")).toArray(new WASInstallation[0]));
            setServers(
                    req.bindJSONToList(
                            WASServer.class,
                            formData.get("wasserver")).toArray(new WASServer[0]));

            save();

            return true;
        }

        /**
         * Checks if the installation folder is valid.
         */
        public FormValidation doCheckHome(@QueryParameter File value) {
            if(value == null || value.getPath().length() == 0) {
                return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("InstallationFolderMustBeSet"));
            }

            if(!value.isDirectory()) {
                return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("NotAFolder", value));
            }

            // let's check for the wsadmin file existence
            if(Hudson.isWindows()) {
                boolean noWsadminBat = false;

                File wsadminFile = new File(value, "bin\\" + WSADMIN_BAT);
                if(!wsadminFile.exists()) {
                    noWsadminBat = true;
                }

                if(noWsadminBat) {
                    return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("NotAWASInstallationFolder", value));
                }
            }
            else {
                boolean noWsadminSh = false;

                File wsadminFile = new File(value, "bin/" + WSADMIN_SH);
                if(!wsadminFile.exists()) {
                    noWsadminSh = true;
                }

                if(noWsadminSh) {
                    return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("NotAWASInstallationFolder", value));
                }
            }

            return FormValidation.ok();
        }

        // --- WASServer checks ---

        public FormValidation doCheckName(@QueryParameter String value) {
            if(value == null || value.length() == 0) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("NameMustBeSet"));
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckConntype(@QueryParameter String value) {
            if(value == null) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("ConntypeMustBeSet"));
            }

            if(!Arrays.asList(WASServer.CONNTYPES).contains(value)) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("InvalidConntype", value));
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckHost(@QueryParameter String value) throws IOException, ServletException {
            if(value == null || value.length() == 0) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostMustBeSet"));
            }

            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(value);
            }
            catch(UnknownHostException uhe) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostNotValid", value));
            }
            catch(SecurityException se) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostSecurityException", value));
            }

            try {
                if(!inetAddress.isReachable(1000)) {
                    throw new IOException();
                }
            }
            catch(IOException ioe) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostCantBeReached", value));
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckPort(@QueryParameter String value) {
            if(value == null || value.length() == 0) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("PortMustBeSet"));
            }
            
            int port;
            try {
                port = Integer.parseInt(value);
                if(port < 0 || port > 65535) {
                    return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("PortMustBeInteger"));
                }
            }
            catch(NumberFormatException nfe) {
                return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("PortMustBeInteger"));
            }
            
            if(port < 1024 || port > 49151) {
                return FormValidation.warning(ResourceBundleHolder.get(WASServer.class).format("PortNotPreferredValue", port));
            }
            
            return FormValidation.ok();
        }

        public FormValidation doCheckUser(@QueryParameter String value) {
            if(value == null || value.length() == 0) {
                return FormValidation.warning(ResourceBundleHolder.get(WASServer.class).format("UserMustBeSetIfSecurityEnabled"));
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckPassword(@QueryParameter String value) {
            if(value == null || value.length() == 0) {
                return FormValidation.warning(ResourceBundleHolder.get(WASServer.class).format("PasswordMustBeSetIfSecurityEnabled"));
            }

            return FormValidation.ok();
        }

    }

}
