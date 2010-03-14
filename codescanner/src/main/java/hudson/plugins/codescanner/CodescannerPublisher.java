package hudson.plugins.codescanner;

import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.Launcher.LocalLauncher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.Priority;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Maximilian Odendahl
 */
// CHECKSTYLE:COUPLING-OFF
public class CodescannerPublisher extends HealthAwarePublisher {

    /** Unique ID of this class. */
    private static final long serialVersionUID = -5936973521277401765L;
    /** Descriptor of this publisher. */
    @Extension
    public static final CodescannerDescriptor CODESCANNER_DESCRIPTOR = new CodescannerDescriptor();
    public String sourcecodedir;
    public String executable;

    /**
     * Creates a new instance of <code>WarningPublisher</code>.
     *
     * @param threshold
     *            Annotation threshold to be reached if a build should be
     *            considered as unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be
     *            considered as failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of annotations is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of annotations is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public CodescannerPublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final String sourcecodedir, final String executable,
            final boolean useDeltaValues) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, "UTF-8", useDeltaValues, "CODESCANNER");

        this.sourcecodedir = sourcecodedir;
        this.executable = executable;
    }
    // CHECKSTYLE:ON

    /**
     * Creates a new parser set for old versions of this class.
     *
     * @return this
     */
    @Override
    protected Object readResolve() {
        super.readResolve();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new CodescannerProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {

        ParserResult project;
        project = new ParserResult(build.getWorkspace());

        try {
            LineIterator iterator = null;
            Pattern pattern = Pattern.compile("([^\\(]+)\\(([0-9]+)\\) : (?:(info|warning|error|note))?: ([a-zA-Z0-9]+): (?:(low|medium|high))?: ([a-zA-Z0-9]+): (.*)");

            if (!executable.equalsIgnoreCase("")) {
                logger.log("Starting CodeScanner...");
                Launcher laucher = new LocalLauncher(TaskListener.NULL);
                final String cmd = "cmd /C " + executable + " " + sourcecodedir;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Proc proc = laucher.launch(cmd, build.getEnvVars(), out, build.getWorkspace());
                proc.join();
                iterator = IOUtils.lineIterator(new ByteArrayInputStream(out.toByteArray()), "UTF-8");
            } else {
                logger.log("Using precreated result file from:");
                String path = build.getWorkspace() + "\\output.txt";
                logger.log(path);
                FileReader reader = new FileReader(path);
                iterator = IOUtils.lineIterator(reader);
            }

            while (iterator.hasNext()) {
                Matcher matcher = pattern.matcher(iterator.nextLine());
                while (matcher.find()) {

                    String fileName = matcher.group(1);
                    int line = Integer.parseInt(matcher.group(2));
                    String category = matcher.group(3);
                    String shorts = matcher.group(4);
                    String prio = matcher.group(5);
                    String types = matcher.group(6);
                    String message = matcher.group(7);

                    Priority priority = Priority.HIGH;
                    if ("medium".equalsIgnoreCase(prio)) {
                        priority = Priority.NORMAL;
                    } else if ("high".equalsIgnoreCase(prio)) {
                        priority = Priority.HIGH;
                    } else if ("low".equalsIgnoreCase(prio)) {
                        priority = Priority.LOW;
                    }
                    project.addAnnotation(new Warning(fileName, line, types, category, message, priority));
                }
            }
            iterator.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("IOException!");
            project.addErrorMessage("IOException!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.log("InterruptedException!");
            project.addErrorMessage("InterruptedException");
        } catch (Exception e) {
            e.printStackTrace();
            logger.log("General Exception!");
            project.addErrorMessage("General Exception");
        }

        CodescannerResult result = new CodescannerResult(build, getDefaultEncoding(), project);
        build.getActions().add(new CodescannerResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return CODESCANNER_DESCRIPTOR;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean canContinue(final Result result) {
        return super.canContinue(result);
    }
}
