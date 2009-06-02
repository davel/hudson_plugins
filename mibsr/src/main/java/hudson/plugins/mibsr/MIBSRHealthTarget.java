package hudson.plugins.mibsr;

import hudson.plugins.mibsr.health.HealthTarget;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 18-Mar-2008 Time: 06:11:01 To change this template use File | Settings
 * | File Templates.
 */
public class MIBSRHealthTarget
    extends HealthTarget<MIBSRHealthMetrics, MIBSRBuildIndividualReport>
{

    @DataBoundConstructor
    public MIBSRHealthTarget( MIBSRHealthMetrics metric, String healthy, String unhealthy, String unstable )
    {
        super( metric, healthy, unhealthy, unstable );
    }
}
