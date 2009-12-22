package hudson.plugins.codescanner;

import hudson.Extension;
import hudson.Launcher;
import hudson.Launcher.LocalLauncher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.AnnotationsClassifier;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.Priority;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Maximilian Odendahl
 */
// CHECKSTYLE:COUPLING-OFF
public class CodescannerPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -5936973521277401764L;

    /** Descriptor of this publisher. */
    @Extension
    public static final CodescannerDescriptor CODESCANNER_DESCRIPTOR = new CodescannerDescriptor();

    /** Ant file-set pattern of files to work with. */
    private final String pattern;
    /** Ant file-set pattern of files to include to report. */
    private final String includePattern;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;

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
     * @param pattern
     *            Ant file-set pattern that defines the files to scan for
     * @param includePattern
     *            Ant file-set pattern of files to include in report
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param canScanConsole
     *            Determines whether the console should be scanned.
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public CodescannerPublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit,
            final String pattern, final String includePattern, final String excludePattern,
            final String defaultEncoding) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, "UTF-8", "WARNINGS");
        this.pattern = pattern;
        this.includePattern = StringUtils.stripToNull(includePattern);
        this.excludePattern = StringUtils.stripToNull(excludePattern);
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

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the Ant file-set pattern of files to include in report.
     *
     * @return Ant file-set pattern of files to include in report
     */
    public String getIncludePattern() {
        return includePattern;
    }

    /**
     * Returns the Ant file-set pattern of files to exclude from report.
     *
     * @return Ant file-set pattern of files to exclude from report
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new CodescannerProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        final String codescanner = "Carbide.c++\\plugins\\com.nokia.carbide.cpp.codescanner_1.4.0.v200911050858_60\\Tools\\codescanner.exe";
        final String cmd = "cmd /C H:\\" + codescanner + " H:\\sf\\app\\organizer\\";
        logger.log(cmd);
        ByteArrayOutputStream out = new ByteArrayOutputStream();


        ParserResult project;
        project = new ParserResult(build.getProject().getWorkspace());

        try {
            Launcher laucher = new LocalLauncher(TaskListener.NULL);
            Proc proc = laucher.launch(cmd, build.getEnvVars(), out, build.getProject().getWorkspace());
//            logger.log(out.toString());
            int exitCode = proc.join();
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("IOException!");
            project.addErrorMessage("IOException!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.log("InterruptedException!");
            project.addErrorMessage("InterruptedException");
        }

        Pattern patterrrr = Pattern.compile("([^\\(]+)\\(([0-9]+)\\) : (?:(info|warning|error|note))?: ([a-zA-Z0-9]+): (?:(low|medium|high))?: ([a-zA-Z0-9]+): (.*)");
        LineIterator iterator = IOUtils.lineIterator(new ByteArrayInputStream(out.toByteArray()), "UTF-8");
        while (iterator.hasNext()) {
            String testss = iterator.nextLine();
            Matcher matcher = patterrrr.matcher(testss);
            logger.log(testss);
            while (matcher.find()) {
                Priority priority = Priority.HIGH;
                            
                String fileName = matcher.group(1);
                int line = Integer.parseInt(matcher.group(2));
                String category = matcher.group(3);
                String shorts = matcher.group(4); 
                String prio = matcher.group(5);
                String types = matcher.group(6);
                String message = matcher.group(7);
        

                if ("medium".equalsIgnoreCase(prio)) {
                    priority = Priority.NORMAL;
                } else if ("high".equalsIgnoreCase(prio)) {
                    priority = Priority.HIGH;
                } else if ("low".equalsIgnoreCase(prio)) {
                    priority = Priority.LOW;
                }

                    project.addAnnotation(new Warning(fileName, line,types, category, message, priority));

            }
        }
        iterator.close();

        project = build.getProject().getWorkspace().act(new AnnotationsClassifier(project, getDefaultEncoding()));
        CodescannerResult result = new CodescannerResultBuilder().build(build, project, getDefaultEncoding());
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
