package hudson.plugins.dry;

import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.Action;
import hudson.plugins.dry.parser.DuplicationParserRegistry;
import hudson.plugins.dry.util.FilesParser;
import hudson.plugins.dry.util.HealthAwareMavenReporter;
import hudson.plugins.dry.util.ParserResult;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the duplicate code analysis (maven 2 project type).
 *
 * @author Ulli Hafner
 */
public class DryReporter extends HealthAwareMavenReporter {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2272875032054063496L;
    /** Descriptor of this publisher. */
    public static final DryReporterDescriptor DRY_SCANNER_DESCRIPTOR = new DryReporterDescriptor(DryPublisher.DRY_DESCRIPTOR);
    /** Default DRY pattern. */
    private static final String DRY_XML_FILE = "cpd.xml";

    /**
     * Creates a new instance of <code>PmdReporter</code>.
     *
     * @param threshold
     *            Bug threshold to be reached if a build should be considered as
     *            unstable.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     */
    @DataBoundConstructor
    public DryReporter(final String threshold, final String healthy, final String unHealthy, final String height, final String thresholdLimit) {
        super(threshold, healthy, unHealthy, height, thresholdLimit, "DRY");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean acceptGoal(final String goal) {
        return "cpd".equals(goal) || "site".equals(goal);
    }

    /** {@inheritDoc} */
    @Override
    public ParserResult perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final PrintStream logger) throws InterruptedException, IOException {
        FilesParser dryCollector = new FilesParser(logger, DRY_XML_FILE, new DuplicationParserRegistry(), true, false);

        return getTargetPath(pom).act(dryCollector);
    }

    /** {@inheritDoc} */
    @Override
    protected void persistResult(final ParserResult project, final MavenBuild build) {
        DryResult result = new DryResultBuilder().build(build, project, getDefaultEncoding());
        build.getActions().add(new MavenDryResultAction(build, this, getHeight(), getDefaultEncoding(), result));
        build.registerAsProjectAction(DryReporter.this);
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final MavenModule module) {
        return new DryProjectAction(module, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends Action> getResultActionClass() {
        return MavenDryResultAction.class;
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporterDescriptor getDescriptor() {
        return DRY_SCANNER_DESCRIPTOR;
    }
}

