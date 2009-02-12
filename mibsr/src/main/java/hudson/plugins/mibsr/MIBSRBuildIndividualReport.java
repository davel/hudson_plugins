package hudson.plugins.mibsr;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.plugins.mibsr.parser.BuildJobs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:15:05
 */
public class MIBSRBuildIndividualReport
    extends AbstractBuildReport<AbstractBuild<?, ?>>
    implements AggregatableAction
{

    private HealthReport healthReport;

    public MIBSRBuildIndividualReport( BuildJobs results )
    {
        super( results );
    }

    /**
     * Write-once setter for property 'build'.
     *
     * @param build The value to set the build to.
     */
    @Override
    public synchronized void setBuild( AbstractBuild<?, ?> build )
    {
        super.setBuild( build );
        if ( this.getBuild() != null )
        {
            getTotals().setOwner( this.getBuild() );
        }
    }

    /**
     * {@inheritDoc}
     */
    public MavenAggregatedReport createAggregatedAction( MavenModuleSetBuild build,
                                                         Map<MavenModule, List<MavenBuild>> moduleBuilds )
    {
        return new MIBSRBuildAggregatedReport( build, moduleBuilds );
    }

    /**
     * {@inheritDoc}
     */
    public HealthReport getBuildHealth()
    {
        return healthReport;
    }

    public void setBuildHealth( HealthReport healthReport )
    {
        this.healthReport = healthReport;
    }
}
