package hudson.plugins.mibsr;

import hudson.model.AbstractBuild;
import hudson.model.HealthReportingAction;
import hudson.plugins.mibsr.GraphHelper;
import hudson.plugins.mibsr.parser.BuildJobs;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import org.apache.maven.plugin.invoker.model.BuildJob;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 09-Jan-2008 21:19:37
 */
public abstract class AbstractBuildReport<T extends AbstractBuild<?, ?>>
    implements HealthReportingAction, Serializable
{
    private final BuildJobs result;

    /**
     * Unique identifier for this class.
     */
    private static final long serialVersionUID = 31415926L;

    /**
     * The owner of this Action.  Ideally I'd like this to be final and set in the constructor, but Maven does not
     * let us do that, so we need a setter.
     */
    private T build = null;

    /**
     * Constructs a new AbstractBuildReport.
     */
    public AbstractBuildReport( BuildJobs result )
    {
        this.result = result;
    }

    public Collection<BuildJob> getResults()
    {
        return result.getBuildJobs();
    }

    public BuildJob getDynamic( String name, StaplerRequest req, StaplerResponse resp )
    {
        return result.getDynamic( name, req, resp );
    }


    public BuildJobs getTotals()
    {
        return result;
    }

    /**
     * The summary of this build report for display on the build index page.
     *
     * @return
     */
    public String getSummary()
    {
        AbstractBuild<?, ?> prevBuild = getBuild().getPreviousBuild();
        while ( prevBuild != null && prevBuild.getAction( getClass() ) == null )
        {
            prevBuild = prevBuild.getPreviousBuild();
        }
        if ( prevBuild == null )
        {
            return result.toSummary();
        }
        else
        {
            AbstractBuildReport action = prevBuild.getAction( getClass() );
            return result.toSummary( action.getTotals() );
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName()
    {
        return PluginImpl.ICON_FILE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName()
    {
        return PluginImpl.DISPLAY_NAME;
    }

    /**
     * Getter for property 'graphName'.
     *
     * @return Value for property 'graphName'.
     */
    public String getGraphName()
    {
        return PluginImpl.GRAPH_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName()
    {
        return PluginImpl.URL;
    }

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph( StaplerRequest req, StaplerResponse rsp )
        throws IOException
    {
        if ( GraphHelper.isGraphUnsupported() )
        {
            GraphHelper.redirectWhenGraphUnsupported( rsp, req );
            return;
        }

        Calendar t = getBuild().getTimestamp();

        if ( req.checkIfModified( t, rsp ) )
        {
            return; // up to date
        }

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        populateDataSetBuilder( dataSetBuilder );

        ChartUtil.generateGraph( req, rsp, GraphHelper.buildChart( dataSetBuilder.build(), "# of tests" ),
                                 getGraphWidth(), getGraphHeight() );
    }


    /**
     * Returns <code>true</code> if there is a graph to plot.
     *
     * @return Value for property 'graphAvailable'.
     */
    public boolean isGraphActive()
    {
        AbstractBuild<?, ?> build = getBuild();
        // in order to have a graph, we must have at least two points.
        int numPoints = 0;
        while ( numPoints < 2 )
        {
            if ( build == null )
            {
                return false;
            }
            if ( build.getAction( getClass() ) != null )
            {
                numPoints++;
            }
            build = build.getPreviousBuild();
        }
        return true;
    }

    protected void populateDataSetBuilder( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset )
    {
        for ( AbstractBuild<?, ?> build = getBuild(); build != null; build = build.getPreviousBuild() )
        {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( build );
            AbstractBuildReport action = build.getAction( getClass() );
            if ( action != null )
            {
                dataset.add( action.getTotals().getPassCount(), "Successful", label );
                dataset.add( action.getTotals().getSkipCount(), "Skipped", label );
                dataset.add( action.getTotals().getErrorCount(), "In error", label );
                dataset.add( action.getTotals().getFailInitCount(), "Failed before run", label );
                dataset.add( action.getTotals().getFailRunCount(), "Failed during run", label );
                dataset.add( action.getTotals().getFailValidateCount(), "Failed after run", label );
            }
        }
    }

    /**
     * Getter for property 'graphWidth'.
     *
     * @return Value for property 'graphWidth'.
     */
    public int getGraphWidth()
    {
        return 500;
    }

    /**
     * Getter for property 'graphHeight'.
     *
     * @return Value for property 'graphHeight'.
     */
    public int getGraphHeight()
    {
        return 200;
    }

    /**
     * Getter for property 'build'.
     *
     * @return Value for property 'build'.
     */
    public synchronized T getBuild()
    {
        return build;
    }

    /**
     * Write once setter for property 'build'.
     *
     * @param build Value to set for property 'build'.
     */
    public synchronized void setBuild( T build )
    {
        // Ideally I'd prefer to use and AtomicReference... but I'm unsure how it would work with the serialization fun
        if ( this.build == null && this.build != build )
        {
            this.build = build;
        }
    }

    /**
     * Override to control when the floating box should be displayed.
     *
     * @return <code>true</code> if the floating box should be visible.
     */
    public boolean isFloatingBoxActive()
    {
        return true;
    }
}
