package com.mtvi.plateng.hudson.springbeandoc;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Project;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class SpringBeanDocArchiver extends Publisher {
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

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException {
        listener.getLogger().println(Messages.SpringBeanDocArchiver_Publishing());

        FilePath javadoc = build.getParent().getWorkspace().child(beandocDir);
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

    public Action getProjectAction(Project project) {
        return new SpringBeanDocAction(project);
    }

    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

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

        public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException,
                ServletException, InterruptedException {
            new DirectoryBrowserSupport(this, project.getDisplayName() + " javadoc").serveFile(req,
                    rsp, new FilePath(getBeandocDir(project)), "help.gif", false);
        }
    }

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private DescriptorImpl() {
            super(SpringBeanDocArchiver.class);
        }

        public String getDisplayName() {
            return Messages.SpringBeanDocArchiver_DisplayName();
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public void doCheck(StaplerRequest req, StaplerResponse rsp) throws IOException,
                ServletException {
            new FormFieldValidator.WorkspaceDirectory(req, rsp).process();
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

}
