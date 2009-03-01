package hudson.plugins.cppunit;

import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.maven.agent.AbortException;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Publisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Class that records CppUnit test reports into Hudson.
 * 
 */
public class CppUnitPublisher extends hudson.tasks.Publisher implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

    private String testResultsPattern;
    private boolean debug = false;
    private boolean keepJUnitReports = false;
    private boolean skipJUnitArchiver = false;

    public CppUnitPublisher(String testResultsPattern, boolean debug, boolean keepJUnitReports, boolean skipJUnitArchiver) {
        this.testResultsPattern = testResultsPattern;
        this.debug = debug;
        if (this.debug) {
            this.keepJUnitReports = keepJUnitReports;
            this.skipJUnitArchiver = skipJUnitArchiver;
        }
    }

    public String getTestResultsPattern() {
        return testResultsPattern;
    }

    public boolean getDebug() {
        return debug;
    }

    public boolean getKeepJunitReports() {
        return keepJUnitReports;
    }

    public boolean getSkipJunitArchiver() {
        return skipJUnitArchiver;
    }

    @Override
    public Action getProjectAction(hudson.model.Project project) {
        TestResultProjectAction action = project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return new TestResultProjectAction(project);
        } else {
            return null;
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
    	
        if (debug) {
            listener.getLogger().println("CppUnit publisher running in debug mode.");
        }
        boolean result = true;
        try {
            listener.getLogger().println("Recording CppUnit tests results");
            CppUnitArchiver archiver = new CppUnitArchiver(listener, testResultsPattern);
            result = build.getProject().getWorkspace().act(archiver);

            if (result) {
                if (skipJUnitArchiver) {
                    listener.getLogger().println("Skipping feeding JUnit reports to JUnitArchiver");
                } else {
                    // Run the JUnit test archiver
                    result = recordTestResult(CppUnitArchiver.JUNIT_REPORTS_PATH + "/TEST-*.xml", build, listener);
                }
                
                if (keepJUnitReports) {
                    listener.getLogger().println("Skipping deletion of temporary JUnit reports.");
                } else {
                    build.getProject().getWorkspace().child(CppUnitArchiver.JUNIT_REPORTS_PATH).deleteRecursive();
                }
            }
            
        } catch (TransformerException te) {
            throw new AbortException("Could not read the XSL XML file.",te);
        }

        return result;
    }

    /**
     * Record the test results into the current build.
     * @param junitFilePattern
     * @param build
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean recordTestResult(String junitFilePattern, AbstractBuild<?, ?> build, BuildListener listener)
            throws InterruptedException, IOException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();

            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }
            TestResult result = getTestResult(junitFilePattern, build, existingTestResults, buildTime);

            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }
            if(result.getPassCount()==0 && result.getFailCount()==0)
                new AbortException("None of the test reports contained any result");
        } catch (AbortException e) {
            if(build.getResult()==Result.FAILURE)
                // most likely a build failed before it gets to the test phase.
                // don't report confusing error message.
                return true;

            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        if (existingAction == null) {
            build.getActions().add(action);
        }

        if(action.getResult().getFailCount()>0)
            build.setResult(Result.UNSTABLE);

        return true;
    }

    /**
     * Collect the test results from the files
     * @param junitFilePattern
     * @param build
     * @param existingTestResults existing test results to add results to
     * @param buildTime
     * @return a test result
     * @throws IOException
     * @throws InterruptedException
     */
    private TestResult getTestResult(final String junitFilePattern, AbstractBuild<?, ?> build,
            final TestResult existingTestResults, final long buildTime) throws IOException, InterruptedException {
        TestResult result = build.getProject().getWorkspace().act(new FileCallable<TestResult>() {
            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                FileSet fs = Util.createFileSet(ws,junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();

                String[] files = ds.getIncludedFiles();
                if(files.length==0) {
                    // no test result. Most likely a configuration error or fatal problem
                    throw new AbortException("No test report files were found. Configuration error?");
                }
                if (existingTestResults == null) {
                    return new TestResult(buildTime, ds);
                } else {
                    existingTestResults.parse(buildTime, ds);
                    return existingTestResults;
                }
            }
        });
        return result;
    }

    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    public static class DescriptorImpl extends Descriptor<Publisher> {

        protected DescriptorImpl() {
            super(CppUnitPublisher.class);
        }

        @Override
        public String getDisplayName() {
            return "Publish CppUnit test result report";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/cppunit/help.html";
        }

        @Override
        public Publisher newInstance(StaplerRequest req) throws FormException {
            return new CppUnitPublisher(req.getParameter("cppunit_reports.pattern"), 
                    (req.getParameter("cppunit_reports.debug") != null), 
                    (req.getParameter("cppunit_reports.keepjunitreports") != null), 
                    (req.getParameter("cppunit_reports.skipjunitarchiver") != null));
        }
    }
}
