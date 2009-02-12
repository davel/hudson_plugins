package hudson.plugins.mibsr;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.plugins.mibsr.parser.BuildJobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:12:12
 */
public class MIBSRBuildAggregatedReport
    extends AbstractBuildReport<MavenModuleSetBuild>
    implements MavenAggregatedReport
{
    private HealthReport buildHealth = null;

    public MIBSRBuildAggregatedReport( MavenModuleSetBuild build, Map<MavenModule, List<MavenBuild>> moduleBuilds )
    {
        super( new BuildJobs() );
        setBuild( build );
    }

    private synchronized void calculateTotals( Map<MavenModule, List<MavenBuild>> moduleBuilds )
    {
        getTotals().clear();
        for ( Map.Entry<MavenModule, List<MavenBuild>> childList : moduleBuilds.entrySet() )
        {
            MavenBuild child = childList.getValue().iterator().next();
            if ( child != null )
            {
                update( moduleBuilds, child );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void update( Map<MavenModule, List<MavenBuild>> moduleBuilds, MavenBuild newBuild )
    {
        MIBSRBuildIndividualReport report = newBuild.getAction( MIBSRBuildIndividualReport.class );
        if ( report != null )
        {
            getTotals().addAll( report.getResults() );
            buildHealth = HealthReport.min( buildHealth, report.getBuildHealth() );
        }
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends AggregatableAction> getIndividualActionType()
    {
        return MIBSRBuildIndividualReport.class;
    }

    /**
     * {@inheritDoc}
     */
    public Action getProjectAction( MavenModuleSet moduleSet )
    {
        for ( MavenModuleSetBuild build : moduleSet.getBuilds() )
        {
            if ( build.getAction( MIBSRBuildAggregatedReport.class ) != null )
            {
                return new MIBSRProjectAggregatedReport( moduleSet );
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public HealthReport getBuildHealth()
    {
        return buildHealth;
    }

}
