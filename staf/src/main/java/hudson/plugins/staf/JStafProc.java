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

import hudson.Launcher;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher.LocalLauncher;
import hudson.model.JDK;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Which;
import hudson.util.ArgumentListBuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * JStafProx helps in the creation of a JVM with all of the correct STAF
 * requirements for using the JSTAF libraries.  These are the correct
 * PATH and LD_LIBRARY_PATHs, the correct jni libraries, etc.
 *
 * This than then be used to execute Callable methods inside of that
 * launched JVM.
 */
public class JStafProc {

    private Launcher launcher;
    private TaskListener listener;
    private EnvVars env;
    private STAFInstallation stafInstallation;
    private JDK jdkInstallation;


    public JStafProc(Launcher launcher, TaskListener listener, EnvVars env, STAFInstallation stafInstallation, JDK jdkInstallation) {
        if(stafInstallation == null) {
            throw new NullPointerException("staf installation can not be null");
        }
        this.launcher = launcher;
        this.listener = listener;
        this.env = env;
        this.stafInstallation = stafInstallation;
        this.jdkInstallation = jdkInstallation;
    }

    public TaskListener getListener() {
        return listener;
    }

    private String javaExe() {
        if(launcher.isUnix()) {
            return "/bin/java";
        }
        else {
            return "\\bin\\java.exe";
        }
    }

    /**
     * Starts a new environment with required STAF parameters, execute a closure, and shut it down.
     */
    public <V,T extends Throwable> V execute(final Callable<V, T> closure) throws T, IOException, InterruptedException {
        VirtualChannel ch = start();
        try {
            return ch.call(closure);
        } finally {
            ch.close();
            ch.join(3000); // give some time for orderly shutdown, but don't block forever.
        }
    }

    private VirtualChannel start() throws IOException, InterruptedException {
        String javaExe;

        if(jdkInstallation != null) {
            javaExe = jdkInstallation.getHome() + javaExe();
        }
        else {
            javaExe = System.getProperty("java.home") + javaExe();
        }
        File slaveJar = Which.jarFile(hudson.remoting.Launcher.class);

        ArgumentListBuilder args = new ArgumentListBuilder().add(javaExe);

        args.add("-Djava.library.path=" + stafInstallation.getLibraryDir());

        if(slaveJar.isFile())
            args.add("-jar").add(slaveJar);
        else // in production code this never happens, but during debugging this is convenientud
            args.add("-cp").add(slaveJar).add(hudson.remoting.Launcher.class.getName());

        String[] cmdArray = args.toCommandArray();

        // a fix recently went in to Hudson to remove the requirement for a
        // wrapping class.  When that version is released, this should be changed
        // to use launcher passed in on the constructor
        return new JStafLocalLauncher(listener).launchChannel(
                cmdArray,
                listener.getLogger(),
                null,
                env);
    }

    static class JStafLocalLauncher extends LocalLauncher {
        public JStafLocalLauncher(TaskListener listener) {
            super(listener);
        }

        public Channel launchChannel(String[] cmd, OutputStream out, FilePath workDir, Map<String,String> overrideEnvVars) throws IOException {
            printCommandLine(cmd, workDir);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            Map<String, String>environment = pb.environment();

            environment.putAll(overrideEnvVars);

            pb.directory(toFile(workDir));

            return launchChannel(out, pb);
        }

        private File toFile(FilePath f) {
            return f==null ? null : new File(f.getRemote());
        }

    }
}
