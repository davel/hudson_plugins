package hudson.plugins.pmd;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.pmd.util.HealthDescriptor;
import hudson.plugins.pmd.util.TrendReportSize;

import java.util.List;
import java.util.Map;

/**
 * A {@link PmdResultAction} for native maven jobs. This action
 * additionally provides result aggregation for sub-modules and for the main
 * project.
 *
 * @author Ulli Hafner
 */
public class MavenPmdResultAction extends PmdResultAction implements AggregatableAction, MavenAggregatedReport {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1273798369273225973L;
    /** Determines the height of the trend graph. */
    private final String height;

    /**
     * Creates a new instance of <code>MavenPmdResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param height
     *            the height of the trend graph
     */
    public MavenPmdResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final String height) {
        super(owner, healthDescriptor);
        this.height = height;
    }

    /**
     * Creates a new instance of <code>MavenPmdResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param height
     *            the height of the trend graph
     * @param result
     *            the result in this build
     */
    public MavenPmdResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final String height, final PmdResult result) {
        super(owner, healthDescriptor, result);
        this.height = height;
    }

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new MavenPmdResultAction(build, getHealthDescriptor(), height);
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new PmdProjectAction(moduleSet, new TrendReportSize(height).getHeight());
    }

    /** {@inheritDoc} */
    public Class<? extends AggregatableAction> getIndividualActionType() {
        return getClass();
    }

    /**
     * Called whenever a new module build is completed, to update the
     * aggregated report. When multiple builds complete simultaneously,
     * Hudson serializes the execution of this method, so this method
     * needs not be concurrency-safe.
     *
     * @param moduleBuilds
     *      Same as <tt>MavenModuleSet.getModuleBuilds()</tt> but provided for convenience and efficiency.
     * @param newBuild
     *      Newly completed build.
     */
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        setResult(new PmdResultBuilder().build(getOwner(), createAggregatedResult(moduleBuilds)));
    }
}

