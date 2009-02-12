package hudson.plugins.mibsr;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.plugins.helpers.AbstractProjectAction;
import hudson.plugins.helpers.GraphHelper;
import hudson.plugins.mibsr.parser.BuildJobs;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 09-Jan-2008 21:22:45
 */
public abstract class AbstractProjectReport<T extends AbstractProject<?, ?>>
    extends AbstractProjectAction<T>
    implements ProminentProjectAction
{

    public AbstractProjectReport( T project )
    {
        super( project );
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName()
    {
        for ( AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild() )
        {
            final AbstractBuildReport action = build.getAction( getBuildActionClass() );
            if ( action != null )
            {
                return PluginImpl.ICON_FILE_NAME;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName()
    {
        for ( AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild() )
        {
            final AbstractBuildReport action = build.getAction( getBuildActionClass() );
            if ( action != null )
            {
                return PluginImpl.DISPLAY_NAME;
            }
        }
        return null;
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
        for ( AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild() )
        {
            final AbstractBuildReport action = build.getAction( getBuildActionClass() );
            if ( action != null )
            {
                return PluginImpl.URL;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getSearchUrl()
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

        Calendar t = getProject().getLastBuild().getTimestamp();

        if ( req.checkIfModified( t, rsp ) )
        {
            return; // up to date
        }

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        populateDataSetBuilder( dataSetBuilder );

        ChartUtil.generateGraph( req, rsp, GraphHelper.buildChart( dataSetBuilder.build() ), getGraphWidth(),
                                 getGraphHeight() );
    }

    /**
     * Returns <code>true</code> if there is a graph to plot.
     *
     * @return Value for property 'graphAvailable'.
     */
    public boolean isGraphActive()
    {
        AbstractBuild<?, ?> build = getProject().getLastBuild();
        // in order to have a graph, we must have at least two points.
        int numPoints = 0;
        while ( numPoints < 2 )
        {
            if ( build == null )
            {
                return false;
            }
            if ( build.getAction( getBuildActionClass() ) != null )
            {
                numPoints++;
            }
            build = build.getPreviousBuild();
        }
        return true;
    }

    /**
     * Returns the latest results.
     *
     * @return Value for property 'graphAvailable'.
     */
    public Collection<BuildJobs> getResults()
    {
        for ( AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild() )
        {
            final AbstractBuildReport action = build.getAction( getBuildActionClass() );
            if ( action != null )
            {
                return action.getResults();
            }
        }
        return Collections.emptySet();
    }

    /**
     * Returns the latest totals.
     *
     * @return Value for property 'graphAvailable'.
     */
    public BuildJobs getTotals()
    {
        for ( AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild() )
        {
            final AbstractBuildReport action = build.getAction( getBuildActionClass() );
            if ( action != null )
            {
                return action.getTotals();
            }
        }
        return null;
    }

    protected abstract Class<? extends AbstractBuildReport> getBuildActionClass();

    protected void populateDataSetBuilder( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset )
    {

        for ( AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild() )
        {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( build );
            AbstractBuildReport action = build.getAction( getBuildActionClass() );
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

}
