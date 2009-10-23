package com.mtvi.plateng.hudson.springbeandoc;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class SpringBeanDocArchiver extends Notifier {
    /**
     * Path to the Spring Beandoc directory in the workspace.
     */
    private final String beandocDir;

    @DataBoundConstructor
    public SpringBeanDocArchiver(String beandoc_dir) {
        this.beandocDir = beandoc_dir;
    }

    public String getBeandocDir() {
        return beandocDir;
    }

    /**
     * Gets the directory where the Spring Beandoc is stored for the given
     * project.
     */
    private static File getBeandocDir(AbstractItem project) {
        return new File(project.getRootDir(), "beandoc");
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException {
        listener.getLogger().println(Messages.SpringBeanDocArchiver_Publishing());

        FilePath javadoc = build.getWorkspace().child(beandocDir);
        FilePath target = new FilePath(getBeandocDir(build.getParent()));

        try {
            // if the build has failed, then there's not much point in reporting
            // an error
            // saying javadoc directory doesn't exist. We want the user to focus
            // on the real error,
            // which is the build failure.
            if (build.getResult().isWorseOrEqualTo(Result.FAILURE) && !javadoc.exists())
                return true;

            javadoc.copyRecursiveTo("**/*", target);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError(Messages.SpringBeanDocArchiver_UnableToCopy(
                    javadoc, target)));
            build.setResult(Result.FAILURE);
        }

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new SpringBeanDocAction(project);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public static class SpringBeanDocAction implements ProminentProjectAction {
        private final AbstractItem project;

        public SpringBeanDocAction(AbstractItem project) {
            this.project = project;
        }

        public String getUrlName() {
            return "springbeandoc";
        }

        public String getDisplayName() {
            if (new File(getBeandocDir(project), "beandoc.properties").exists())
                return Messages.SpringBeanDocArchiver_DisplayName_SpringBeanDoc();
            else
                return Messages.SpringBeanDocArchiver_DisplayName_Generic();
        }

        public String getIconFileName() {
            if (getBeandocDir(project).exists()) {
                return "/plugin/springbeandoc/images/24x24/spring-logo.png";
            } else {
                // hide it since we don't have beandoc yet.
                return null;
            }
        }

        public DirectoryBrowserSupport doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException,
                ServletException, InterruptedException {
            return new DirectoryBrowserSupport(this, new FilePath(getBeandocDir(project)),
                    project.getDisplayName() + " javadoc", "help.gif", false);
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(SpringBeanDocArchiver.class);
        }

        public String getDisplayName() {
            return Messages.SpringBeanDocArchiver_DisplayName();
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public FormValidation doCheck(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            FilePath ws = project.getSomeWorkspace();
            return ws != null ? ws.validateRelativeDirectory(value) : FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

}
