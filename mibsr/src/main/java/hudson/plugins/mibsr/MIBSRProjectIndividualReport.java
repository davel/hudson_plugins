package hudson.plugins.mibsr;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 22:05:48
 */
public class MIBSRProjectIndividualReport
    extends AbstractProjectReport<AbstractProject<?, ?>>
    implements ProminentProjectAction
{
    public MIBSRProjectIndividualReport( AbstractProject<?, ?> project )
    {
        super( project );
    }

    protected Class<? extends AbstractBuildReport> getBuildActionClass()
    {
        return MIBSRBuildIndividualReport.class;
    }
}
