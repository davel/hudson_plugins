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
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * SCons installation.
 *
 * @author Gregory Boissinot
 */
public final class SConsInstallation extends ToolInstallation implements NodeSpecific<SConsInstallation>, EnvironmentSpecific<SConsInstallation> {


    @DataBoundConstructor
    public SConsInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    public SConsInstallation forEnvironment(EnvVars environment) {
        return new SConsInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public SConsInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new SConsInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    public String getSconsExecutable(Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new Callable<String, IOException>() {
            public String call() throws IOException {
                // Get where the exec should be, then make sure it exists
                File execFileName = new File(Util.replaceMacro(getHome(), EnvVars.masterEnvVars));

                if (execFileName.exists()) {
                    return execFileName.getPath();
                } else {
                    return null;
                }
            }
        });
    }


    @Extension
    public static class DescriptorImpl extends ToolDescriptor<SConsInstallation> {
        @CopyOnWrite
        private volatile SConsInstallation[] installations = new SConsInstallation[0];

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return ResourceBundleHolder.get(SConsBuilderScriptFile.class).format("DisplayName");
        }

        @Override
        public SConsInstallation[] getInstallations() {
            return installations.clone();
        }

        @Override
        public void setInstallations(SConsInstallation[] installs) {
            installations = installs;
            save();
        }

        /**
         * Checks if the installation folder is valid.
         */
        public FormValidation doCheckHome(@QueryParameter File value) {

            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
                return FormValidation.ok();

            if (value.getPath().equals("")) {
                return FormValidation.error(Messages.scons_InstallationFolderMustBeSet());
            }

            if (!value.isFile()) {
                return FormValidation.error(Messages.scons_NotAFile());
            }

            return FormValidation.ok();
        }
    }

}
