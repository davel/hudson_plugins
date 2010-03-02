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

package com.thalesgroup.hudson.plugins.copyarchiver;

import com.thalesgroup.hudson.plugins.copyarchiver.util.CopyArchiverLogger;
import hudson.*;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Gregory Boissinot
 */
public class CopyArchiverPublisher extends Notifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sharedDirectoryPath;

    private boolean useTimestamp;

    private String datePattern;

    private boolean flatten;

    private boolean deleteShared;

    private List<ArchivedJobEntry> archivedJobList = new ArrayList<ArchivedJobEntry>();

    @SuppressWarnings("unused")
    public String getSharedDirectoryPath() {
        return sharedDirectoryPath;
    }

    public void setSharedDirectoryPath(String sharedDirectoryPath) {
        this.sharedDirectoryPath = sharedDirectoryPath;
    }

    @SuppressWarnings("unused")
    public boolean getUseTimestamp() {
        return useTimestamp;
    }

    @SuppressWarnings("unused")
    public boolean getFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public void setUseTimestamp(boolean useTimestamp) {
        this.useTimestamp = useTimestamp;
    }

    public List<ArchivedJobEntry> getArchivedJobList() {
        return archivedJobList;
    }

    public void setArchivedJobList(List<ArchivedJobEntry> archivedJobList) {
        this.archivedJobList = archivedJobList;
    }

    @SuppressWarnings("unused")
    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    @SuppressWarnings("unused")
    public boolean getDeleteShared() {
        return deleteShared;
    }

    public void setDeleteShared(boolean deleteShared) {
        this.deleteShared = deleteShared;
    }


    @Extension
    public static final class CopyArchiverDescriptor extends BuildStepDescriptor<Publisher> {

        //CopyOnWriteList
        private List<AbstractProject> jobs;

        public CopyArchiverDescriptor() {
            super(CopyArchiverPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.copyArchiver_displayName();
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            CopyArchiverPublisher pub = new CopyArchiverPublisher();
            req.bindParameters(pub, "copyarchiver.");
            List<ArchivedJobEntry> archivedJobEntries = req.bindParametersToList(ArchivedJobEntry.class, "copyarchiver.entry.");
            pub.getArchivedJobList().addAll(archivedJobEntries);
            return pub;
        }


        @Override
        public String getHelpFile() {
            return "/plugin/copyarchiver/help.html";
        }


        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @SuppressWarnings("unused")
        public List<AbstractProject> getJobs() {
            return Hudson.getInstance().getItems(AbstractProject.class);
        }

        public FormValidation doDateTimePatternCheck(@QueryParameter("value") String pattern) {
            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return FormValidation.ok();
            if (pattern == null || pattern.trim().length() == 0) {
                return FormValidation.error("You must provide a pattern value");
            }

            try {
                new SimpleDateFormat(pattern);
            } catch (NullPointerException npe) {
                return FormValidation.error("Invalid input: " + npe.getMessage());
            } catch (IllegalArgumentException iae) {
                return FormValidation.error("Invalid input: " + iae.getMessage());
            }

            return FormValidation.ok();
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           final BuildListener listener) throws InterruptedException, IOException {

        if (build.getResult().equals(Result.UNSTABLE) || build.getResult().equals(Result.SUCCESS)) {

            final AbstractProject project = build.getProject();

            try {
                if (useTimestamp) {
                    if (datePattern == null || datePattern.trim().length() == 0) {
                        build.setResult(Result.FAILURE);
                        throw new AbortException("The option 'Change the date format' is activated. You must provide a new date pattern.");
                    }
                }

                final FilePath destDirFilePath = new FilePath(new File(filterField(build, listener, sharedDirectoryPath)));
                final boolean isMatrixExcutorProject = MatrixConfiguration.class.isAssignableFrom(project.getClass());
                Boolean result = build.getWorkspace().act(new FilePath.FileCallable<Boolean>() {
                    public Boolean invoke(File f, VirtualChannel channel) throws IOException {

                        try {

                            CopyArchiverLogger.log(listener, "Copying archived artifacts in the shared directory '" + destDirFilePath + "'.");

                            if (!destDirFilePath.isDirectory())
                                destDirFilePath.mkdirs();

                            if (deleteShared) {

                                if (isMatrixExcutorProject) {
                                    CopyArchiverLogger.log(listener, "[WARNING] - The type of the current job is matrix project");
                                    CopyArchiverLogger.log(listener, "[WARNING] - In this special case, the delete operation is not supported.");
                                    CopyArchiverLogger.log(listener, "[WARNING] - Checking 'Delete the shared directory.' option has no effect.");
                                    CopyArchiverLogger.log(listener, "[WARNING] - Uncheck 'Delete the shared directory.' option and manaed the shared directory in an other way.");
                                    return true;
                                } else {
                                    destDirFilePath.deleteRecursive();
                                }
                            }

                            return true;
                        }
                        catch (InterruptedException ie) {
                            return false;
                        }
                    }
                });

                if (!result) {
                    CopyArchiverLogger.log(listener, "An error has been occured during the copyarchiver process.");
                    build.setResult(Result.FAILURE);
                    return false;
                }

                FilePathArchiver lastSuccessfulDirFilePathArchiver = null;
                int numCopied = 0;

                for (ArchivedJobEntry archivedJobEntry : archivedJobList) {
                    AbstractProject selectedProject = (AbstractProject) Hudson.getInstance().getItem(archivedJobEntry.jobName);

                    if (isSameProject(project, selectedProject)) {
                        lastSuccessfulDirFilePathArchiver = new FilePathArchiver(build.getWorkspace());
                    } else {
                        File lastSuccessfulDir;
                        if (MatrixProject.class.isAssignableFrom(selectedProject.getClass())) {
                            MatrixProject matrixProject = (MatrixProject) selectedProject;
                            MatrixBuild matrixBuild = matrixProject.getLastSuccessfulBuild();
                            if (matrixBuild == null) {
                                CopyArchiverLogger.log(listener, "The selected project has never built. No copy will be proceded.");
                                return true;
                            }
                            lastSuccessfulDir = matrixBuild.getArtifactsDir();
                        } else {
                            Run run = selectedProject.getLastSuccessfulBuild();
                            if (run == null) {
                                CopyArchiverLogger.log(listener, "The selected has never built. No copy will be proceded.");
                                return true;
                            }
                            lastSuccessfulDir = run.getArtifactsDir();
                        }
                        lastSuccessfulDirFilePathArchiver = new FilePathArchiver(new FilePath(launcher.getChannel(), lastSuccessfulDir.getAbsolutePath()));
                    }

                    //Copy
                    numCopied += lastSuccessfulDirFilePathArchiver.copyRecursiveTo(flatten, filterField(build, listener, archivedJobEntry.pattern), filterField(build, listener, archivedJobEntry.excludes), destDirFilePath);

                }
                CopyArchiverLogger.log(listener, "'" + numCopied + "' artifacts have been copied.");
                CopyArchiverLogger.log(listener, "Stop copying archived artifacts in the shared directory.");

                return true;


            }
            catch (Exception e) {
                CopyArchiverLogger.log(listener, "Error on copyarchiver analysis: " + e);
                build.setResult(Result.FAILURE);
                return false;
            }
        }

        return true;

    }

    private boolean isSameProject(AbstractProject executorProject, AbstractProject selectedProject) {
        boolean matrixSelected = MatrixProject.class.isAssignableFrom(selectedProject.getClass());
        boolean matrixExecutor = MatrixConfiguration.class.isAssignableFrom(executorProject.getClass());

        if (matrixExecutor && !matrixSelected) {
            return false;
        }

        if (matrixExecutor && matrixSelected) {
            return (executorProject.getParent()).equals(selectedProject);
        }

        if (!matrixExecutor && matrixSelected) {
            return false;
        }

        return selectedProject.equals(executorProject);

    }


    private String filterField(AbstractBuild<?, ?> build, BuildListener listener, String fieldText) throws InterruptedException, IOException {
        String str = null;

        Map<String, String> vars = new HashMap<String, String>();
        Set<Map.Entry<String, String>> set = build.getEnvironment(listener).entrySet();
        for (Map.Entry<String, String> entry : set) {
            vars.put(entry.getKey(), entry.getValue());
        }

        if (useTimestamp && datePattern != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
            final String newBuildIdStr = sdf.format(build.getTimestamp().getTime());
            vars.put("BUILD_ID", newBuildIdStr);
        }
        str = Util.replaceMacro(fieldText, vars);
        str = Util.replaceMacro(str, build.getBuildVariables());
        return str;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}

