package hudson.plugins.coverage;

import java.util.List;
import java.util.Map;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.plugins.coverage.model.Measurement;
import hudson.plugins.coverage.model.Metric;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 29-Jun-2008 21:00:10
 */
public class CoverageBuildIndividualReport extends AbstractBuildReport<AbstractBuild<?, ?>>
        implements AggregatableAction {

    private HealthReport healthReport;

    public CoverageBuildIndividualReport(Map<Metric, Measurement> results) {
        super(results);
    }

    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild build,
                                                        Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new CoverageBuildAggregatedReport(build, moduleBuilds);
    }

    /**
     * {@inheritDoc}
     */
    public HealthReport getBuildHealth() {
        return healthReport;
    }

    public void setBuildHealth(HealthReport healthReport) {
        this.healthReport = healthReport;
    }
}
