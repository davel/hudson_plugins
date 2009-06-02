package hudson.plugins.mibsr;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.plugins.mibsr.health.HealthMetric;
import hudson.plugins.mibsr.parser.BuildJobs;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:24:06
 */
public class MIBSRPublisher
    extends Publisher
{

    private String reportFilenamePattern;

    private MIBSRHealthTarget[] targets;

    @DataBoundConstructor
    public MIBSRPublisher( String reportFilenamePattern, MIBSRHealthTarget[] targets )
    {
        reportFilenamePattern.getClass();
        this.reportFilenamePattern = reportFilenamePattern;
        this.targets = targets == null ? new MIBSRHealthTarget[0] : targets;
    }

    public String getReportFilenamePattern()
    {
        return reportFilenamePattern;
    }

    public MIBSRHealthTarget[] getTargets()
    {
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsToRunAfterFinalized()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean perform( final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener )
        throws InterruptedException, IOException
    {

        MIBSRBuildIndividualReport action =
            build.getProject().getModuleRoot().act( new Worker( build, listener, reportFilenamePattern ) );
        if ( action != null )
        {
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

            BuildJobs results = action.getTotals();

            if ( results.getFailCount() > 0 || results.getErrorCount() > 0 )
            {
                build.setResult( Result.UNSTABLE );
            }
        }

        return true;  // never stop the build
    }

    /**
     * {@inheritDoc}
     */
    public boolean prebuild( AbstractBuild<?, ?> build, BuildListener listener )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Action getProjectAction( AbstractProject<?, ?> project )
    {
        return new MIBSRProjectIndividualReport( project );
    }

    @Extension
    public static final class DescriptorImpl
        extends BuildStepDescriptor<Publisher>
    {

        public DescriptorImpl()
        {
            super( MIBSRPublisher.class );
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName()
        {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        public Publisher newInstance( StaplerRequest req, JSONObject formData )
            throws FormException
        {
            ConvertUtils.register( MIBSRHealthMetrics.CONVERTER, MIBSRHealthMetrics.class );
            return req.bindJSON( MIBSRPublisher.class, formData );
        }

        public boolean isApplicable( Class<? extends AbstractProject> aClass )
        {
            return MatrixProject.class.isAssignableFrom( aClass )
                || MatrixConfiguration.class.isAssignableFrom( aClass ) || FreeStyleProject.class.isAssignableFrom(
                aClass );
        }

        public HealthMetric[] getMetrics()
        {
            return MIBSRHealthMetrics.values();
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public void doCheck( StaplerRequest req, StaplerResponse rsp )
            throws IOException, ServletException
        {
            new FormFieldValidator.WorkspaceFileMask( req, rsp ).process();
        }

        public String applyDefaultIncludes( String invokerResults )
        {
            if ( invokerResults == null || invokerResults.trim().length() == 0 )
            {
                return "**/target/invoker-reports/BUILD-*.xml";
            }
            else
            {
                return invokerResults.trim();
            }
        }

    }

    private static class Worker
        implements FilePath.FileCallable<MIBSRBuildIndividualReport>, Serializable
    {
        private final AbstractBuild<?, ?> build;

        private final BuildListener listener;

        private String reportFilenamePattern;

        public Worker( AbstractBuild<?, ?> build, BuildListener listener, String reportFilenamePattern )
        {
            this.build = build;
            this.listener = listener;
            this.reportFilenamePattern = reportFilenamePattern;
        }

        public MIBSRBuildIndividualReport invoke( File file, VirtualChannel virtualChannel )
            throws IOException
        {
            FilePath[] paths;
            try
            {
                paths = build.getProject().getModuleRoot().list( reportFilenamePattern );
            }
            catch ( InterruptedException e )
            {
                IOException ioe = new IOException( e.getMessage() );
                ioe.initCause( e );
                throw ioe;
            }

            String[] fileNames = new String[paths.length];
            for ( int i = 0; i < paths.length; i++ )
            {
                fileNames[i] = paths[i].getRemote();
            }

            return new MIBSRBuildIndividualReport( BuildJobs.parse( fileNames ) );
        }
    }
}
