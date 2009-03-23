package hudson.plugins.testabilityexplorer.report;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.plugins.testabilityexplorer.publisher.MavenPublisher;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A {@link MavenAggregatedReport} that will update an individual build report every time a maven
 * module is finished.
 *
 * @author reik.schatz
 */
public class BuildAggregatedReport extends AbstractBuildReport<MavenModuleSetBuild> implements
        MavenAggregatedReport {

    public BuildAggregatedReport(MavenModuleSetBuild build, Collection<Statistic> results,
            ReportBuilder reportBuilder, CostDetailBuilder detailBuilder) {
        super(results, reportBuilder, detailBuilder);
        setBuild(build);
    }

    @Override
    void addResults(Collection<Statistic> statistics) {
        if (!(null == getBuild() || null == getBuild().getProject()
                || null == getBuild().getProject().getReporters())) {
            MavenPublisher publisher = getBuild().getProject().getReporters().get(
                    MavenPublisher.class);
            if (null != publisher && publisher.getAggregateFiles()) {
                mergeStatistics(statistics, publisher.getWeightFactor());
                return;
            }
        }
        super.addResults(statistics);
    }

    /** {@inheritDoc} */
    public synchronized void update(Map<MavenModule, List<MavenBuild>> moduleBuilds,
            MavenBuild newBuild) {
        BuildIndividualReport report = newBuild.getAction(BuildIndividualReport.class);

        if (report != null) {
            Collection<Statistic> moduleResults = report.getResults();
            addResults(moduleResults);
        }
    }

    /** {@inheritDoc} */
    public Class<? extends AggregatableAction> getIndividualActionType() {
        return BuildIndividualReport.class;
    }

    /** {@inheritDoc} */
    public Action getProjectAction(MavenModuleSet moduleSet) {
        for (MavenModuleSetBuild build : moduleSet.getBuilds()) {
            if (build.getAction(BuildAggregatedReport.class) != null) {
                return new ProjectAggregatedReport(moduleSet);
            }
        }
        return null;
    }
}
