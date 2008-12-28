package hudson.plugins.testabilityexplorer.report;

import hudson.maven.MavenModuleSet;
import hudson.model.ProminentProjectAction;

/**
 * A {@link AbstractProjectReport} used for maven module sets in M2 projects.
 *
 * @author reik.schatz
 */
public class ProjectAggregatedReport extends AbstractProjectReport<MavenModuleSet> implements ProminentProjectAction
{
    public ProjectAggregatedReport(MavenModuleSet project)
    {
        super(project);
    }

    /** {@inheritDoc} */
    protected Class<? extends AbstractBuildReport> getBuildActionClass()
    {
        return BuildAggregatedReport.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFloatingBoxActive()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGraphActive()
    {
        return true;
    }
}
