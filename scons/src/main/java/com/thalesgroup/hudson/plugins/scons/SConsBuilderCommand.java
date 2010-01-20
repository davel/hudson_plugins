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
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;


public class SConsBuilderCommand extends SConsAbstractBuilder {


    private final String commandScript;

    @DataBoundConstructor
    public SConsBuilderCommand(String sconsName, String options, String variables, String targets, String rootSconsscriptDirectory, String commandScript) {
        super(sconsName, options, variables, targets, rootSconsscriptDirectory);
        this.commandScript = commandScript;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        ArgumentListBuilder args = new ArgumentListBuilder();
        EnvVars env = build.getEnvironment(listener);

        //Build the scons executable and fill in the args buffer
        if (buildSconsExecutable(launcher, listener, args, env)) return false;


        String normalizedOptions = getOptions().replaceAll("[\t\r\n]+", " ");
        String normalizedFileVariables = getVariables().replaceAll("[\t\r\n]+", " ");
        String normalizedTargets = getTargets().replaceAll("[\t\r\n]+", " ");

        if (normalizedOptions != null && normalizedOptions.trim().length() != 0) {
            args.addTokenized(normalizedOptions);
        }


        //Determines the rootDirectory
        String normalizedRootSconsscriptDirectory = getRootSconsscriptDirectory().replaceAll("[\t\r\n]+", " ");
        FilePath rootSconsscriptFilePath = build.getModuleRoot();
        if (normalizedRootSconsscriptDirectory != null && normalizedRootSconsscriptDirectory.trim().length() != 0) {
            normalizedRootSconsscriptDirectory = Util.replaceMacro(normalizedRootSconsscriptDirectory, build.getEnvironment(listener));
            rootSconsscriptFilePath = new FilePath(rootSconsscriptFilePath, normalizedRootSconsscriptDirectory);
        }

        FilePath dynamicSconsFile = rootSconsscriptFilePath.createTextTempFile("scons", ".generated", getCommandScript());
        args.add("-f");
        args.add(dynamicSconsFile.getName());


        if (normalizedRootSconsscriptDirectory != null && normalizedRootSconsscriptDirectory.trim().length() != 0) {
            args.add("-C");
            args.add(normalizedRootSconsscriptDirectory);
        }

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
            e.printStackTrace(listener.fatalError(Messages.scons_commandExecutionFailed()));
            return false;
        }
        finally{
            dynamicSconsFile.delete();
        }
    }


    @Extension
    public static final SConsBuilderCommandDescriptor DESCRIPTOR = new SConsBuilderCommandDescriptor();

    public SConsBuilderDescriptor getDescritor() {
        return DESCRIPTOR;
    }


    public static class SConsBuilderCommandDescriptor extends SConsBuilderDescriptor {

        public SConsBuilderCommandDescriptor() {
            load();
        }

        protected SConsBuilderCommandDescriptor(Class<? extends SConsBuilderScriptFile> clazz) {
            super(clazz);
        }

        @Override
        public String getDisplayName() {
            return Messages.scons_builderCommand_displayName();
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
