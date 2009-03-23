package hudson.plugins.testabilityexplorer.report;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Connects a {@link Statistic} with a {@link AbstractBuild}.
 *
 * @author reik.schatz
 */
public class BuildIndividualReport extends AbstractBuildReport<AbstractBuild<?, ?>> implements AggregatableAction
{
    public BuildIndividualReport(Collection<Statistic> results, ReportBuilder reportBuilder, CostDetailBuilder detailBuilder)
    {
        super(results, reportBuilder, detailBuilder);
    }

    @Override
    public synchronized void setBuild(AbstractBuild<?, ?> build) {
        super.setBuild(build);
        if (this.getBuild() != null) {
            for (Statistic r : getResults()) {
                r.setOwner(this.getBuild());
            }
        }
    }

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild mavenModuleSetBuild, Map<MavenModule, List<MavenBuild>> mavenModuleListMap)
    {
        return new BuildAggregatedReport(mavenModuleSetBuild, getResults(), getReportBuilder(), getDetailBuilder());
    }
}
