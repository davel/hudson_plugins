package hudson.plugins.mibsr;

import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.plugins.helpers.AbstractPublisherImpl;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.helpers.health.HealthMetric;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:24:06
 */
public class MIBSRPublisher
    extends AbstractPublisherImpl
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

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * {@inheritDoc}
     */
    public Descriptor<Publisher> getDescriptor()
    {
        return DESCRIPTOR;
    }

    /**
     * {@inheritDoc}
     */
    public Action getProjectAction( AbstractProject<?, ?> project )
    {
        return new MIBSRProjectIndividualReport( project );
    }

    protected Ghostwriter newGhostwriter()
    {
        return new MIBSRGhostwriter( reportFilenamePattern, targets );
    }

    public static final class DescriptorImpl
        extends BuildStepDescriptor<Publisher>
    {

        /**
         * Do not instantiate DescriptorImpl.
         */
        private DescriptorImpl()
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
            return !MavenModuleSet.class.isAssignableFrom( aClass ) && !MavenModule.class.isAssignableFrom( aClass );
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
                return "**/target/invoker-reports/INVOCATION-*.xml";
            }
            else
            {
                return invokerResults.trim();
            }
        }

    }

}
