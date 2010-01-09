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

import hudson.CopyOnWrite;
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
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Corresponds to an STAF / STAX installation.  
 *
 * <p>To use a {@link STAF} build step, it is mandatory to define a installation:
 * No default installations can be assumed.</p>
 */
public class STAFInstallation extends ToolInstallation implements NodeSpecific<STAFInstallation>, EnvironmentSpecific<STAFInstallation> {

    private static final long serialVersionUID = 1L;

    private static final String WINDOWS_JSTAF_PATH = "bin/JSTAF.jar";
    private static final String UNIX_JSTAF_PATH = "lib/JSTAF.jar";
    private static final String WINDOWS_STAF_EXEC = "bin/STAF.exe";
    private static final String UNIX_STAF_EXEC = "bin/STAF";

    // Required environment vars
    private static final String LD_LIBRARY_PATH_ENV = "LD_LIBRARY_PATH";
    private static final String DYLD_LIBRARY_PATH_ENV = "DYLD_LIBRARY_PATH";
    private static final String STAF_CONV_DIR_ENV = "STAFCONVDIR";
    private static final String CLASSPATH_ENV = "CLASSPATH";

    @DataBoundConstructor
    public STAFInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    public STAFInstallation forEnvironment(EnvVars environment) {
        return new STAFInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public STAFInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new STAFInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    public String getStafExecutable(Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new Callable<String,IOException>() {
            public String call() throws IOException {
                // Get where the exec should be, then make sure it exists
                File execFileName = getExecFile();
                if(execFileName.exists()) {
                    return execFileName.getPath();
                }
                else {
                    return null;
                }
            }
        });
    }

    public Map<String, String> getRequiredEnvVars() {
        HashMap<String, String> result = new HashMap<String, String>(3);
        String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);

        result.put(LD_LIBRARY_PATH_ENV, getLibraryDir());
        result.put(DYLD_LIBRARY_PATH_ENV, getLibraryDir());
        result.put(STAF_CONV_DIR_ENV, home + "/codepage");
        result.put(CLASSPATH_ENV, home + "/" + getJStafJarFile());

        return result;
    }

    public String getLibraryDir() {
        String librariesLocation;

        // an install on Windows puts all of the .dlls into the
        // bin directory.  But on Mac/Linux, the .so / .jnilib files
        // are put in the lib directory
        if(Hudson.isWindows()) {
            librariesLocation = "\\bin";
        }
        else {
            librariesLocation = "/lib";
        }

        String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
        return home + librariesLocation;
    }

    private File getExecFile() {
        String execFileName;

        if(Hudson.isWindows()) {
            execFileName = WINDOWS_STAF_EXEC;
        }
        else {
            execFileName = UNIX_STAF_EXEC;
        }

        return new File(Util.replaceMacro(getHome(), EnvVars.masterEnvVars), execFileName);
    }

    private File getJStafJarFile() {
        String jarFileName;

        // As with the libraries, for some reason the STAF installer
        // puts the jars in different places between Win / Mac / Linux
        if(Hudson.isWindows()) {
            jarFileName = WINDOWS_JSTAF_PATH;
        }
        else {
            jarFileName = UNIX_JSTAF_PATH;
        }

        return new File(Util.replaceMacro(getHome(), EnvVars.masterEnvVars), jarFileName);
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<STAFInstallation> {
        @CopyOnWrite
        private volatile STAFInstallation[] installations = new STAFInstallation[0];

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return ResourceBundleHolder.get(STAF.class).format("DisplayName");
        }

        @Override
        public STAFInstallation[] getInstallations() {
            return installations.clone();
        }

        @Override
        public void setInstallations(STAFInstallation[] installs) {
            installations = installs;
            save();
        }

        /**
         * Checks if the installation folder is valid.
         */
        public FormValidation doCheckHome(@QueryParameter File value) {
            if(!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
                return FormValidation.ok();

            if(value.getPath().equals("")) {
                return FormValidation.error(ResourceBundleHolder.get(STAFInstallation.class).format("InstallationFolderMustBeSet"));
            }

            if(!value.isDirectory()) {
                return FormValidation.error(ResourceBundleHolder.get(STAFInstallation.class).format("NotAFolder", value));
            }

            // let's check for the STAF files
            // are assumption will be if the staf executable and the 
            // JSTAF jar exists, then everything else will be OK
            if(Hudson.isWindows()) {
                File stafExe = new File(value, WINDOWS_STAF_EXEC);
                File jstafJar = new File(value, WINDOWS_JSTAF_PATH);
                
                if(!stafExe.exists() || !jstafJar.exists()) {
                    return FormValidation.error(ResourceBundleHolder.get(STAFInstallation.class).format("NotASTAFInstallationFolder", value));
                }
            }
            else {
                File stafExe = new File(value, UNIX_STAF_EXEC);
                File jstafJar = new File(value, UNIX_JSTAF_PATH);
                
                if(!stafExe.exists() || !jstafJar.exists()) {
                    return FormValidation.error(ResourceBundleHolder.get(STAFInstallation.class).format("NotASTAFInstallationFolder", value));
                }
            }

            return FormValidation.ok();
        }

    }

}
