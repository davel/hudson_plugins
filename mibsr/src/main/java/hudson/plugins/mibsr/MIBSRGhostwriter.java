package hudson.plugins.mibsr;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.plugins.helpers.BuildProxy;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.mibsr.parser.BuildJobs;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 23:16:52
 */
public class MIBSRGhostwriter
    implements Ghostwriter, Ghostwriter.MasterGhostwriter, Ghostwriter.SlaveGhostwriter
{

    private final String reportFilenamePattern;

    private final MIBSRHealthTarget[] targets;

    public MIBSRGhostwriter( String reportFilenamePattern, MIBSRHealthTarget... targets )
    {
        this.reportFilenamePattern = reportFilenamePattern;
        this.targets = targets;
    }

    public boolean performFromMaster( AbstractBuild<?, ?> build, FilePath executionRoot, BuildListener listener )
        throws InterruptedException, IOException
    {
        return true;
    }

    public boolean performFromSlave( BuildProxy build, BuildListener listener )
        throws InterruptedException, IOException
    {
        FilePath[] paths = build.getExecutionRootDir().list( reportFilenamePattern );
        String[] fileNames = new String[paths.length];
        for ( int i = 0; i < paths.length; i++ )
        {
            fileNames[i] = paths[i].getRemote();
        }

        BuildJobs results = null;
        try
        {
            results = BuildJobs.parse( fileNames );
        }
        catch ( IOException e )
        {
            e.printStackTrace( listener.getLogger() );
        }
        if ( results != null )
        {
            MIBSRBuildIndividualReport action = new MIBSRBuildIndividualReport( results );
            if ( targets != null && targets.length > 0 )
            {
                HealthReport r = null;
                for ( MIBSRHealthTarget target : targets )
                {
                    r = HealthReport.min( r, target.evaluateHealth( action, PluginImpl.DISPLAY_NAME + ": " ) );
                }
                action.setBuildHealth( r );
            }
           
            build.getActions().add( action );
            if ( results.getFailCount() > 0 || results.getErrorCount() > 0 )
            {
                build.setResult( Result.UNSTABLE );
            }
        }
        return true;
    }
}
