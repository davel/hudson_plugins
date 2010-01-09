/*
 * The MIT License
 *
 * Copyright (c) 2010, Gregory Covert Smith
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

package hudson.plugins.staf;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.util.Map;
import net.sf.json.JSONObject;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Defines the STAF build step
 */
public class STAF extends Builder {

    /** Identifies {@link STAFInstallation} to be used. */
    private final String stafInstallationName;
    /**
     * The target endpoint
     */
    private final String endpoint;
    /**
     * The target service
     */
    private final String service;
    /**
     * The request
     */
    private final String request;

    @DataBoundConstructor
    public STAF(String stafInstallationName, String endpoint, String service, String request) {
        this.stafInstallationName = stafInstallationName;
        if(endpoint.length() != 0) {
            this.endpoint = endpoint;
        }
        else {
            this.endpoint = "local";
        }
        this.service = service;
        this.request = request;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Returns the {@link STAFInstallation} to use when the build takes place
     * ({@code null} if none has been set).
     */
    public STAFInstallation getStafInstallation() {
        for (STAFInstallation installation : getDescriptor().getInstallations()) {
            if (getStafInstallationName() != null && installation.getName().equals(getStafInstallationName())) {
                return installation;
            }
        }

        return null;
    }

    public String getStafInstallationName() {
        return stafInstallationName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getService() {
        return service;
    }

    public String getRequest() {
        return request;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        AbstractProject project = build.getProject();
        ArgumentListBuilder args = new ArgumentListBuilder();
        EnvVars env = build.getEnvironment(listener);
        VariableResolver<String> varResolver = build.getBuildVariableResolver();

        // --- STAF installation ---

        // has a STAF installation been set? 
        STAFInstallation stafInstallation = getStafInstallation();
        if (stafInstallation == null) {
            listener.fatalError(ResourceBundleHolder.get(STAF.class).format("NoInstallationSet"));
            return false;
        }

        stafInstallation = stafInstallation.forNode(Computer.currentComputer().getNode(), listener);
        stafInstallation = stafInstallation.forEnvironment(env);

        String stafExecutable = stafInstallation.getStafExecutable(launcher);
        if (stafExecutable == null) {
            listener.fatalError(ResourceBundleHolder.get(STAF.class).format("NoStafExecutable", stafInstallation.getName()));
            return false;
        }

        // add all of the required paramaters for the installation to the env
        Map<String, String> stafEnv = stafInstallation.getRequiredEnvVars();
        env.overrideAll(stafEnv);


        args.add(stafExecutable);

        // add the endpoint
        args.add(getEndpoint());

        // add the service name
        args.add(getService());

        // add all of the request parameters
        String lRequests = Util.replaceMacro(env.expand(getRequest()), varResolver);
        args.addTokenized(lRequests.replaceAll("[\t\r\n]+", " "));

        try {
            int r = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getWorkspace()).join();

            if(r != 0) {
                // handle some known cases for bad error codes to log appropriately
                String errorMessage;
                switch (r){
                    // more should be added.  If there are definite ones to add...
                    case(21):
                        errorMessage = ResourceBundleHolder.get(STAF.class).format("StafServiceNotRunning", r);
                    default:
                        errorMessage = ResourceBundleHolder.get(STAF.class).format("ExecutionResultNotZero", r);
                }

                listener.fatalError(errorMessage);
                return false;
            }
            else {
                return true;
            }
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);

            String errorMessage = ResourceBundleHolder.get(STAF.class).format("ExecutionFailed");

            listener.fatalError(errorMessage);
            return false;
        } 
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        protected DescriptorImpl(Class<? extends STAF> clazz) {
            super(clazz);
        }

        @Override
        public String getDisplayName() {
            return ResourceBundleHolder.get(STAF.class).format("DisplayName");
        }

        public STAFInstallation[] getInstallations() {
            return Hudson.getInstance().getDescriptorByType(STAFInstallation.DescriptorImpl.class).getInstallations();
        }

        /**
         * Returns the {@link STAFInstallation.DescriptorImpl} instance.
         */
        public STAFInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(STAFInstallation.DescriptorImpl.class);
        }

        /**
         * Checks for fields
         */
        public FormValidation doCheckService(@QueryParameter String value) {
            if (value.equals("")) {
                return FormValidation.error(ResourceBundleHolder.get(STAF.class).format("ServiceCannotBeEmpty", value));
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(STAF.class, formData);
        }
    }
}
