package hudson.plugins.mibsr;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.Action;
import hudson.plugins.helpers.AbstractMavenReporterImpl;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.helpers.health.HealthMetric;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:26:06
 */
public class MIBSRMavenPublisher
    extends AbstractMavenReporterImpl
{
    private MIBSRHealthTarget[] targets;

    @DataBoundConstructor
    public MIBSRMavenPublisher( MIBSRHealthTarget... targets )
    {
        this.targets = targets == null ? new MIBSRHealthTarget[0] : targets;
    }

    public MIBSRHealthTarget[] getTargets()
    {
        return targets;
    }

    /**
     * The groupId of the Maven plugin that provides the functionality we want to report on.
     */
    private static final String PLUGIN_GROUP_ID = "org.codehaus.mojo";

    /**
     * The artifactId of the Maven plugin that provides the functionality we want to report on.
     */
    private static final String PLUGIN_ARTIFACT_ID = "mibsr-maven-plugin";

    /**
     * The goal of the Maven plugin that implements the functionality we want to report on.
     */
    private static final String PLUGIN_EXECUTE_GOAL = "report";

    protected boolean isExecutingMojo( MojoInfo mojo )
    {
        return mojo.pluginName.matches( PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID ) && PLUGIN_EXECUTE_GOAL.equals(
            mojo.getGoal() );
    }

    protected Ghostwriter newGhostwriter( MavenProject pom, MojoInfo mojo )
    {
        String tempFileName;
        try
        {
            tempFileName = mojo.getConfigurationValue( "tempFileName", String.class );
        }
        catch ( ComponentConfigurationException e )
        {
            tempFileName = null;
        }
        if ( tempFileName == null )
        {
            tempFileName = "mibsr-raw-report.xml";
        }
        System.out.println( tempFileName );
        File baseDir = pom.getBasedir().getAbsoluteFile();
        File xmlOutputDirectory;
        try
        {
            xmlOutputDirectory = mojo.getConfigurationValue( "xmlOutputDirector", File.class );
        }
        catch ( ComponentConfigurationException e )
        {
            xmlOutputDirectory = null;
        }
        if ( xmlOutputDirectory == null )
        {
            xmlOutputDirectory = new File( pom.getBuild().getDirectory() );
        }
        System.out.println( baseDir );
        System.out.println( xmlOutputDirectory );
        String searchPath;
        String targetPath = makeDirEndWithFileSeparator( fixFilePathSeparator( xmlOutputDirectory.getAbsolutePath() ) );
        String baseDirPath = makeDirEndWithFileSeparator( fixFilePathSeparator( baseDir.getAbsolutePath() ) );
        if ( targetPath.startsWith( baseDirPath ) )
        {
            searchPath = targetPath.substring( baseDirPath.length() ) + tempFileName;
        }
        else
        {
            searchPath = "**/" + tempFileName;
        }

        return new MIBSRGhostwriter( searchPath, targets );
    }

    private String makeDirEndWithFileSeparator( String baseDirPath )
    {
        if ( !baseDirPath.endsWith( File.separator ) )
        {
            baseDirPath += File.separator;
        }
        return baseDirPath;
    }

    private String fixFilePathSeparator( String path )
    {
        return path.replace( File.separatorChar == '/' ? '\\' : '/', File.separatorChar );
    }

    public Action getProjectAction( MavenModule module )
    {
        for ( MavenBuild build : module.getBuilds() )
        {
            if ( build.getAction( MIBSRBuildIndividualReport.class ) != null )
            {
                return new MIBSRProjectIndividualReport( module );
            }
        }
        return null;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * {@inheritDoc}
     */
    public MavenReporterDescriptor getDescriptor()
    {
        return DESCRIPTOR;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static final class DescriptorImpl
        extends MavenReporterDescriptor
    {

        /**
         * Do not instantiate DescriptorImpl.
         */
        private DescriptorImpl()
        {
            super( MIBSRMavenPublisher.class );
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName()
        {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        public MavenReporter newInstance( StaplerRequest req, JSONObject formData )
            throws FormException
        {
            ConvertUtils.register( MIBSRHealthMetrics.CONVERTER, MIBSRHealthMetrics.class );
            return req.bindJSON( MIBSRMavenPublisher.class, formData );
        }

        public HealthMetric[] getMetrics()
        {
            return MIBSRHealthMetrics.values();
        }
    }

}
