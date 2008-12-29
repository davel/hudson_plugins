package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.maven.*;
import hudson.model.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A {@link MavenAggregatedReport} that will update an individual build report every time a maven
 * module is finished.
 *
 * @author reik.schatz
 */
public class BuildAggregatedReport extends AbstractBuildReport<MavenModuleSetBuild> implements MavenAggregatedReport
{
    public BuildAggregatedReport(MavenModuleSetBuild build, Collection<Statistic> results, ReportBuilder reportBuilder, CostDetailBuilder detailBuilder)
    {
        super(results, reportBuilder, detailBuilder);
        setBuild(build);
    }

    /** {@inheritDoc} */
    public synchronized void update(Map<MavenModule, List<MavenBuild>> moduleBuilds, MavenBuild newBuild)
    {
        BuildIndividualReport report = newBuild.getAction(BuildIndividualReport.class);

        if (report != null) {
            Collection<Statistic> moduleResults = report.getResults();
            addResults(moduleResults);
        }
    }

    /** {@inheritDoc} */
    public Class<? extends AggregatableAction> getIndividualActionType()
    {
        return BuildIndividualReport.class;
    }

    /** {@inheritDoc} */
    public Action getProjectAction(MavenModuleSet moduleSet) 
    {
        for (MavenModuleSetBuild build : moduleSet.getBuilds())
        {
            if (build.getAction(BuildAggregatedReport.class) != null)
            {
                return new ProjectAggregatedReport(moduleSet);
            }
        }
        return null;
    }
}
