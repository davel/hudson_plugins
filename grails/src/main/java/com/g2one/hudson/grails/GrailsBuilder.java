package com.g2one.hudson.grails;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormFieldValidator;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrailsBuilder extends Builder {

    private final String targets;
    private final String name;
    private String grailsWorkDir;
    private String projectWorkDir;

    @DataBoundConstructor
    public GrailsBuilder(String targets, String name, String grailsWorkDir, String projectWorkDir) {
        this.name = name;
        this.targets = targets;
        this.grailsWorkDir = grailsWorkDir;
        this.projectWorkDir = projectWorkDir;
    }

    public String getProjectWorkDir() {
        return projectWorkDir;
    }

    public void setProjectWorkDir(String projectWorkDir) {
        this.projectWorkDir = projectWorkDir;
    }

    public String getGrailsWorkDir() {
        return grailsWorkDir;
    }

    public void setGrailsWorkDir(String grailsWorkDir) {
        this.grailsWorkDir = grailsWorkDir;
    }

    public String getName() {
        return name;
    }

    public String getTargets() {
        return targets;
    }

    public GrailsInstallation getGrails() {
        for (GrailsInstallation i : DESCRIPTOR.getInstallations()) {
            if (name != null && i.getName().equals(name))
                return i;
        }
        return null;
    }

    public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        Project proj = build.getProject();
        List<String[]> targetsToRun = getTargetsToRun();
        if (targetsToRun.size() > 0) {
            String execName;
            if (launcher.isUnix()) {
                execName = "grails";
            } else {
                execName = "grails.bat";
            }

            GrailsInstallation grailsInstallation = getGrails();

            Map<String, String> env = build.getEnvVars();
            if (grailsInstallation != null) {
                env.put("GRAILS_HOME", grailsInstallation.getGrailsHome());
            }

            for (String[] targetsAndArgs : targetsToRun) {

                String target = targetsAndArgs[0];
                ArgumentListBuilder args = new ArgumentListBuilder();

                if (grailsInstallation == null) {
                    args.add(execName);
                } else {
                    File exec = grailsInstallation.getExecutable();
                    if (!grailsInstallation.getExists()) {
                        listener.fatalError(exec + " doesn't exist");
                        return false;
                    }
                    args.add(exec.getPath());
                }
                args.addKeyValuePairs("-D", build.getBuildVariables());
                Map sytemProperties = new HashMap();
                if (grailsWorkDir != null && !"".equals(grailsWorkDir.trim())) {
                    sytemProperties.put("grails.work.dir", grailsWorkDir.trim());
                }
                if (projectWorkDir != null && !"".equals(projectWorkDir.trim())) {
                    sytemProperties.put("project.work.dir", projectWorkDir.trim());
                }
                if (sytemProperties.size() > 0) {
                    args.addKeyValuePairs("-D", sytemProperties);
                }

                args.add(target);
                for (int i = 1; i < targetsAndArgs.length; i++) {
                    args.add(targetsAndArgs[i]);
                }

                if (!launcher.isUnix()) {
                    args.prepend("cmd.exe", "/C");
                    args.add("&&", "exit", "%%ERRORLEVEL%%");
                }

                try {
                    int r = launcher.launch(args.toCommandArray(), env, listener.getLogger(), proj.getModuleRoot()).join();
                    if (r != 0) return false;
                } catch (IOException e) {
                    Util.displayIOException(e, listener);
                    e.printStackTrace(listener.fatalError("command execution failed"));
                    return false;
                }
            }
        } else {
            listener.getLogger().println("Error: No Targets To Run!");
            return false;
        }
        return true;
    }

    protected List<String[]> getTargetsToRun() {
        List<String[]> targetsToRun = new ArrayList<String[]>();
        if (targets != null && targets.length() > 0) {
            try {
                JSAP jsap = new JSAP();
                UnflaggedOption option = new UnflaggedOption("targets");
                option.setGreedy(true);
                jsap.registerParameter(option);
                JSAPResult jsapResult = jsap.parse(this.targets);
                String[] targets = jsapResult.getStringArray("targets");
                for (String targetAndArgs : targets) {
                    String[] pieces = targetAndArgs.split(" ");
                    targetsToRun.add(pieces);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return targetsToRun;
    }

    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {

        private volatile GrailsInstallation[] installations = new GrailsInstallation[0];

        DescriptorImpl() {
            super(GrailsBuilder.class);
        }

        public String getDisplayName() {
            return "Build With Grails";
        }

        public boolean configure(StaplerRequest req) throws FormException {
            installations = req.bindParametersToList(GrailsInstallation.class, "grails.").toArray(new GrailsInstallation[0]);
            save();
            return true;
        }

        public Builder newInstance(StaplerRequest req) {
            return req.bindParameters(GrailsBuilder.class, "grails.");
        }


        public GrailsInstallation[] getInstallations() {
            return installations;
        }

        public void doCheckGrailsHome(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator(req, rsp, true) {
                public void check() throws IOException, ServletException {
                    File f = getFileParameter("value");
                    if (!f.isDirectory()) {
                        error(f + " is not a directory");
                        return;
                    }

                    if (!new File(f, "bin/grails").exists() && !new File(f, "bin/grails.bat").exists()) {
                        error(f + " doesn't look like a Grails directory");
                        return;
                    }
                    ok();
                }
            }.process();
        }

    }
}
