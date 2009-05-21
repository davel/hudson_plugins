package hudson.plugins.seleniumhq;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Clover {@link Publisher}.
 * 
 * @author Pascal Martin
 */
public class SeleniumhqPublisher extends Publisher implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * {@link FileSet} "includes" string, like "foo/bar/*.html"
     */
    private final String testResults;

    /**
     * 
     * @param name
     * @stapler-constructor
     */
    public SeleniumhqPublisher(String testResults) {
        this.testResults = testResults;
    }

    public String getTestResults() {
        return testResults;
    }

    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unchecked")
    public Action getProjectAction(Project project) {
        return new SeleniumhqProjectAction(project);
    }

    /** Gets the directory where the Clover Report is stored for the given project. */
    public static File getSeleniumReportDir(AbstractItem project) {
        return new File(project.getRootDir(), "seleniumhq");
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println("Publishing Selenium report...");

        SeleniumhqBuildAction action;

        // clear result directory
        FilePath rootTarget = new FilePath(getSeleniumReportDir(build.getParent()));
        rootTarget.deleteContents();

        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();
            final long nowMaster = System.currentTimeMillis();

            File workspace = new File(build.getProject().getRootDir(), "workspace");
            FilePath workspacePath = new FilePath(workspace);
            TestResult result = workspacePath.act(new FileCallable<TestResult>() {
                private static final long serialVersionUID = 1L;

                public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                    final long nowSlave = System.currentTimeMillis();

                    FileSet fs = Util.createFileSet(ws, testResults);
                    DirectoryScanner ds = fs.getDirectoryScanner();

                    String[] files = ds.getIncludedFiles();
                    if (files.length == 0) {
                        // no test result. Most likely a configuration error or fatal
                        // problem
                        throw new AbortException("No Test Report Found");
                    }

                    return new TestResult(buildTime + (nowSlave - nowMaster), ds);
                }
            });

            if (result.getNumTestTotal() == 0) {
                throw new AbortException("Result does not have test");
            }

            action = new SeleniumhqBuildAction(build, result, listener);

            // Store result file
            List<File> files = result.getFiles();
            if (files.size() == 1) {
                FilePath source = new FilePath(files.get(0));
                source.copyTo(new FilePath(rootTarget, "index.html"));
            } else {
                String header = "<html><head><title>Selenium result</title></head><body><center><br/><h2>Selenium Test Result</h2><ul>";
                String footer = "</ul></center></body></html>";
                OutputStream output = rootTarget.child("index.html").write();
                output.write(header.getBytes());
                int index = 0;
                // Make test index file
                for (File file : files) {
                    FilePath source = new FilePath(file);
                    String dest = index + "/" + source.getName();
                    source.copyTo(new FilePath(rootTarget, dest));
                    String link = "<li><a href=\"" + dest + "\">" + source.getName() + "</a></li>";
                    output.write(link.getBytes());
                    ++index;
                }
                output.write(footer.getBytes());
                output.close();
            }

        } catch (IOException e) {
            listener.error("Failed to archive Selenium reports");
            listener.error(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        } catch (AbortException e) {
            if (build.getResult() != Result.FAILURE) {
                listener.error(e.getMessage());
                build.setResult(Result.FAILURE);
            }
            return true;
        }

        build.getActions().add(action);

        listener.getLogger().println("  Test failures: " + action.getResult().getNumTestFailures());
        listener.getLogger().println("  Test totals  : " + action.getResult().getNumTestTotal());

        if (action.getResult().getNumTestFailures() > 0)
            build.setResult(Result.UNSTABLE);

        return true;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<Publisher> {

        public DescriptorImpl() {
            super(SeleniumhqPublisher.class);
        }

        public String getDisplayName() {
            return "Publish Selenium Report";
        }

        @SuppressWarnings("deprecation")
        public boolean configure(StaplerRequest req) throws FormException {
            req.bindParameters(this, "seleniumhq.");
            save();
            return super.configure(req); // To change body of overridden methods use File | Settings
            // | File Templates.
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public void doCheck(StaplerRequest req, StaplerResponse rsp,
                @QueryParameter final String value) throws IOException, ServletException {
            new FormFieldValidator.WorkspaceFileMask(req, rsp).process();
        }

        /** Creates a new instance of {@link SeleniumhqPublisher} from a submitted form. */
        public SeleniumhqPublisher newInstance(StaplerRequest req) throws FormException {
            SeleniumhqPublisher instance = req.bindParameters(SeleniumhqPublisher.class,
                    "seleniumhq.");
            return instance;
        }

        public String getHelpFile() {
            return "/plugin/seleniumhq/help-publisher.html";
        }

    }
}
