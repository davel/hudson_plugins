/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Martin Eigenbrodt, Peter Hayes
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
package ncover;

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
import hudson.model.Hudson;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Saves NCover coverage for the project and publish them. 
 *
 * @author Kohsuke Kawaguchi
 * @author Mike Rooney
 */
public class NCoverArchiver extends Recorder {
    /**
     * Path to the coverage directory in the workspace.
     */
    private final String coverageDir;

    /**
     * The file to use as the index of the directory.
     */
    private final String indexFileName;

    /**
     * If true, retain coverage for all the successful builds.
     */
    private final boolean keepAll;
        
    @DataBoundConstructor
    public NCoverArchiver(String coverage_dir, String index_file_name, boolean keep_all) {
        this.coverageDir = coverage_dir;
        this.indexFileName = index_file_name;
        this.keepAll = keep_all;
    }

    public String getCoverageDir() {
        return coverageDir;
    }

    public String getIndexFileName() {
	return indexFileName;
    }

    public boolean isKeepAll() {
        return keepAll;
    }

    /**
     * Gets the directory where the NCover coverage is stored for the given project.
     */
    private static File getNCoverDir(AbstractItem project) {
        return new File(project.getRootDir(), "ncover");
    }
    
    private static void writeFile(ArrayList<String> lines, File path) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        for (int i = 0; i < lines.size(); i++) {
            bw.write(lines.get(i));
            bw.newLine();
        }
        bw.close();
        return;
    }

    /**
     * Gets the directory where the NCover coverage is stored for the given build.
     */
    private static File getDir(Run run) {
        return new File(run.getRootDir(),"ncover");
    }

    public ArrayList<String> readFile(String filePath) throws java.io.FileNotFoundException, java.io.IOException {
        ArrayList<String> aList = new ArrayList<String>();
        
        try {
            final InputStream is = this.getClass().getResourceAsStream(filePath);
            try {
                final Reader r = new InputStreamReader(is);
                try {
                    final BufferedReader br = new BufferedReader(r);
                    try {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            aList.add(line);
                        }
                        br.close();
                        r.close();
                        is.close();
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    try {
                        r.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // failure
            e.printStackTrace();
        }
        
        return aList;
    }
    
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        listener.getLogger().println("Publishing NCover HTML report...");

        FilePath ncover = build.getParent().getWorkspace().child(coverageDir);
        FilePath target = new FilePath(keepAll ? getDir(build) : getNCoverDir(build.getProject()));
        
        // Grab the contents of the header and footer as arrays
        ArrayList<String> headerLines;
        ArrayList<String> footerLines;
        try {
            headerLines = readFile("/ncover/NCoverArchiver/header.html");
            footerLines = readFile("/ncover/NCoverArchiver/footer.html");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        
        // The index name might be a comma separated list of names, so let's figure out all the pages we should index.
        // Why yes, this would be one line of Python: reports = [report.strip() for report in indexFileName.split(",") if report]
        String[] csvReports = indexFileName.split(",");
        ArrayList<String> reports = new ArrayList<String>();
        for (int i=0; i<csvReports.length; i++) {
            String report = csvReports[i];
            report = report.trim();
            if (!report.equals("")) {
                reports.add(report);
                listener.getLogger().println("Report: '"+report+"'");
                String tabNo = "tab" + (i+1);
                // Make the report name the filename without the extesion.
                String reportName = report.substring(0, report.lastIndexOf("."));
                String tabItem = "<li id=\""+tabNo+"\" class=\"unselected\" onclick=\"updateBody('"+tabNo+"');\" value=\""+report+"\">"+reportName+"</li>";
                headerLines.add(tabItem);
            }
        }
        // Add the JS to change the link as appopriate.
        headerLines.add("<script type=\"text/javascript\">document.getElementById(\"hudson_link\").href=\"" + hudsonUrl +"\";</script>");
       

        try {
            if (!ncover.exists()) {
                listener.error("Specified NCover directory '" + coverageDir + "' does not exist.");
                build.setResult(Result.FAILURE);
                return true;
            } else if (!keepAll) {
                // We are only keeping one copy at the project level, so remove the old one.
                target.deleteRecursive();
            }
            
            if (ncover.copyRecursiveTo("**/*",target)==0) {
                listener.error("Directory '" + ncover + "' exists but failed copying to '" + target + "'.");
                if(build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
                    // If the build failed, don't complain that there was no coverage.
                    // The build probably didn't even get to the point where it produces coverage.
                    listener.error("This is especially strange since your build otherwise succeeded.");
                }
                build.setResult(Result.FAILURE);
                return true;
            }
        } catch (IOException e) {
            Util.displayIOException(e,listener);
            e.printStackTrace(listener.fatalError("NCover failure"));
            build.setResult(Result.FAILURE);
             return true;
        }
        
        // Add build action, if coverage is recorded for each build
        if(keepAll)
            build.addAction(new NCoverBuildAction(build));
        
        // Now add the footer.
        headerLines.addAll(footerLines);
        // And write this as the index
        try {
            writeFile(headerLines, new File(target.toString(), "coverage-wrapper.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject project) {
        return new NCoverAction(project);
    }

    protected static abstract class BaseNCoverAction implements Action {
        public String getUrlName() {
            return "ncover";
        }

        public String getDisplayName() {
            /*
            if (new File(dir(), "help-doc.html").exists())
                return Messages.JavadocArchiver_DisplayName_Javadoc();
            else
                return Messages.JavadocArchiver_DisplayName_Generic();
            */
            return "Code Coverage";
        }

        public String getIconFileName() {
            return "graph.gif";
        }

        /**
         * Serves NCover coverage.
         */
        public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(dir()), getTitle(), "graph.gif", false);
            dbs.setIndexFileName("coverage-wrapper.html"); // Hudson >= 1.312
            dbs.generateResponse(req,rsp,this);
        }

        protected abstract String getTitle();

        protected abstract File dir();
    }

    public static class NCoverAction extends BaseNCoverAction implements ProminentProjectAction {
        private final AbstractItem project;

        public NCoverAction(AbstractItem project) {
            this.project = project;
        }

        @Override
        protected File dir() {
            // Would like to change AbstractItem to AbstractProject, but is
            // that a backwards compatible change?
            if (project instanceof AbstractProject) {
                AbstractProject abstractProject = (AbstractProject) project;

                Run run = abstractProject.getLastSuccessfulBuild();
                if (run != null) {
                    File javadocDir = getDir(run);

                    if (javadocDir.exists())
                        return javadocDir;
                }
            }

            return getNCoverDir(project);
        }

        @Override
        protected String getTitle() {
            return project.getDisplayName()+" ncover2";
        }
    }
    
    public static class NCoverBuildAction extends BaseNCoverAction {
        private final AbstractBuild<?,?> build;
        
        public NCoverBuildAction(AbstractBuild<?,?> build) {
            this.build = build;
        }

        @Override
        protected String getTitle() {
            return build.getDisplayName()+" ncover3";
        }

        @Override
        protected File dir() {
            return getDir(build);
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            //return Messages.JavadocArchiver_DisplayName();
            return "Publisher NCover HTML report";
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public FormValidation doCheck(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
            FilePath ws = project.getWorkspace();
            return ws != null ? ws.validateRelativeDirectory(value) : FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
