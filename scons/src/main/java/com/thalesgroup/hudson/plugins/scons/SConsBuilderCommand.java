/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.scons;

import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;


public class SConsBuilderCommand extends SConsAbstractBuilder {


    private final String commandScript;

    @DataBoundConstructor
    public SConsBuilderCommand(String sconsName, String options, String variables, String targets, String commandScript) {
        super(sconsName, options, variables, targets);
        this.commandScript = commandScript;
    }


    public SConsInstallation getSconsInstallation() {
        for (SConsInstallation installation : DESCRIPTOR.getInstallations()) {
            if (getSconsName() != null && installation.getName().equals(getSconsName())) {
                return installation;
            }
        }
        return null;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        ArgumentListBuilder args = new ArgumentListBuilder();
        EnvVars env = build.getEnvironment(listener);

        SConsInstallation sconsInstallation = getSconsInstallation();
        if (sconsInstallation == null) {
            args.add("scons");
        }
        else {
            sconsInstallation = sconsInstallation.forNode(Computer.currentComputer().getNode(), listener);
            sconsInstallation = sconsInstallation.forEnvironment(env);

            String sconsExecutable = sconsInstallation.getSconsExecutable(launcher);
            if (sconsExecutable == null) {
                listener.fatalError("No Scons Executable");
                return false;
            }
            args.add(sconsExecutable);
        }


        String normalizedOptions = getOptions().replaceAll("[\t\r\n]+", " ");
        String normalizedFileVariables = getVariables().replaceAll("[\t\r\n]+", " ");
        String normalizedTargets = getTargets().replaceAll("[\t\r\n]+", " ");

        if (normalizedOptions != null && normalizedOptions.trim().length() != 0) {
            args.addTokenized(normalizedOptions);
        }

        File tempDir = Util.createTempDir();
        tempDir.createNewFile();


        FilePath dynamicSconsFile = build.getModuleRoot().createTextTempFile("scons",  ".generated", getCommandScript());


        args.add("-f");
        args.add(dynamicSconsFile.getName());


        if (normalizedFileVariables != null && normalizedFileVariables.trim().length() != 0) {
            args.addTokenized(normalizedFileVariables);
        }

        if (normalizedTargets != null && normalizedTargets.trim().length() != 0) {
            args.addTokenized(normalizedTargets);
        }

        if (!launcher.isUnix()) {
            // on Windows, executing batch file can't return the correct error
            // code,
            // so we need to wrap it into cmd.exe.
            // double %% is needed because we want ERRORLEVEL to be expanded
            // after
            // batch file executed, not before. This alone shows how broken
            // Windows is...
            args.prepend("cmd.exe", "/C");
            args.add("&&", "exit", "%%ERRORLEVEL%%");
        }

        try {
            int r = launcher.launch().cmds(args).envs(build.getEnvironment(listener))
                    .stdout(listener).pwd(build.getModuleRoot()).join();
            return r == 0;
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("command execution failed"));
            return false;
        }
    }


    @Extension
    public static final SConsBuilderCommandDescriptor DESCRIPTOR = new SConsBuilderCommandDescriptor();

    public static class SConsBuilderCommandDescriptor extends BuildStepDescriptor<Builder> {

        public SConsBuilderCommandDescriptor() {
            load();
        }

        protected SConsBuilderCommandDescriptor(Class<? extends SConsBuilderScriptFile> clazz) {
            super(clazz);
        }

        @Override
        public String getDisplayName() {
            return "Provide your scons content";
        }

        public SConsInstallation[] getInstallations() {
            return Hudson.getInstance().getDescriptorByType(SConsInstallation.DescriptorImpl.class).getInstallations();
        }

        /**
         * Returns the {@link SConsInstallation.DescriptorImpl} instance.
         */
        public SConsInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(SConsInstallation.DescriptorImpl.class);
        }

        /**
         * Checks for fields
         */
        public FormValidation doCheckService(@QueryParameter String value) {
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(SConsBuilderCommand.class, formData);
        }
    }

    public String getCommandScript() {
        return commandScript;
    }
}
