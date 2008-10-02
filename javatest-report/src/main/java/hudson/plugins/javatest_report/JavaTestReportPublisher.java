/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package hudson.plugins.javatest_report;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.remoting.VirtualChannel;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Action;
import hudson.tasks.Publisher;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.IOException2;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Rama Pulavarthi
 */
public class JavaTestReportPublisher extends Publisher implements Serializable {
    private final String includes;
    private final String jtwork;

    public JavaTestReportPublisher(String includes,String jtwork) {
        this.includes = includes;
        this.jtwork = jtwork;
    }

    /**
     * Ant "&lt;fileset @includes="..." /> pattern to specify SQE XML files
     */
    public String getIncludes() {
        return includes;
    }

    /**
     * TCK work directory so that JTR files can be accessed.
     */
    public String getJtwork() {
        return jtwork;
    }

    public Action getProjectAction(Project project) {
        return new TestResultProjectAction(project);
    }
    
    /**
     * Indicates an orderly abortion of the processing.
     */
    private static final class AbortException extends RuntimeException {
        public AbortException(String s) {
            super(s);
        }
    }

    public boolean perform(Build build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        final long buildTime = build.getTimestamp().getTimeInMillis();

        archiveJTWork(build,listener);

        listener.getLogger().println("Collecting JavaTest reports");

        // target directory
        File dataDir = JavaTestAction.getDataDir(build);
        dataDir.mkdirs();
        final FilePath target = new FilePath(dataDir);

        try {
            build.getProject().getWorkspace().act(new FileCallable<Void>() {
                public Void invoke(File ws, VirtualChannel channel) throws IOException {
                    FileSet fs = new FileSet();
                    org.apache.tools.ant.Project p = new org.apache.tools.ant.Project();
                    fs.setProject(p);
                    fs.setDir(ws);
                    fs.setIncludes(includes);
                    String[] includedFiles = fs.getDirectoryScanner(p).getIncludedFiles();

                    if(includedFiles.length==0)
                        // no test result. Most likely a configuration error or fatal problem
                        throw new AbortException("No Java test report files were found. Configuration error?");

                    int counter=0;

                    // archive report files.
                    // this is not the most efficient way to do this,
                    // but there usually aren't too many report files, so this works OK in practice.
                    for (String file : includedFiles) {
                        File src = new File(ws, file);

                        if(src.lastModified()<buildTime) {
                            listener.getLogger().println("Skipping "+src+" because it's not up to date");
                            continue;       // not up to date.
                        }

                        try {
                            new FilePath(src).copyTo(target.child("report"+(counter++)+".xml"));
                        } catch (InterruptedException e) {
                            throw new IOException2("aborted while copying "+src,e);
                        }
                    }
                    return null;
                }
                private static final long serialVersionUID = 1L;
            });
        } catch (AbortException e) {
            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true; /// but this is not a fatal error
        }


        JavaTestAction action = new JavaTestAction(build, listener);
        build.getActions().add(action);

        Report r = action.getResult();

        if(r.getTotalCount()==0) {
            listener.getLogger().println("Test reports were found but none of them are new. Did tests run?");
            // no test result. Most likely a configuration error or fatal problem
            build.setResult(Result.FAILURE);
        }

        if(r.getFailCount()>0)
            build.setResult(Result.UNSTABLE);

        return true;
     }

    private void archiveJTWork(Build<?,?> owner, BuildListener listener) throws IOException, InterruptedException {
        if (jtwork == null || jtwork.equals("")) {
            listener.getLogger().println("Set JavaTest Work directory for better reporting");
        } else {
            Project p = owner.getProject();

            p.getWorkspace().child(jtwork).copyRecursiveTo("**/*",
                new FilePath(owner.getArtifactsDir()).child("java-test-work"));
        }
    }

    public Descriptor<Publisher> getDescriptor() {
        return DescriptorImpl.DESCRIPTOR;
    }

    /*package*/ static class DescriptorImpl extends Descriptor<Publisher> {
        public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

        public DescriptorImpl() {
            super(JavaTestReportPublisher.class);
        }

        public String getDisplayName() {
            return "Publish JavaTest result report";
        }

        public String getHelpFile() {
            return "/plugin/javatest-report/help.html";
        }

        public Publisher newInstance(StaplerRequest req) {
            return new JavaTestReportPublisher(req.getParameter("javatest_includes"), req.getParameter("javatest_jtwork"));
        }
    }

    private static final long serialVersionUID = 1L;
}
