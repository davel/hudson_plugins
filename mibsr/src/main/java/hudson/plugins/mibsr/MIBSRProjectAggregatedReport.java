package hudson.plugins.mibsr;

import hudson.maven.MavenModuleSet;
import hudson.model.ProminentProjectAction;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 22:06:11
 */
public class MIBSRProjectAggregatedReport
    extends AbstractProjectReport<MavenModuleSet>
    implements ProminentProjectAction
{
    public MIBSRProjectAggregatedReport( MavenModuleSet project )
    {
        super( project );
    }

    protected Class<? extends AbstractBuildReport> getBuildActionClass()
    {
        return MIBSRBuildAggregatedReport.class;
    }
}
