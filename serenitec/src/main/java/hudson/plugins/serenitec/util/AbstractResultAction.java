/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/24 09:44:14 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;


import hudson.plugins.serenitec.SerenitecDescriptor;
import hudson.plugins.serenitec.util.model.EntriesProvider;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Controls the live cycle of Hudson results. This action persists the results
 * of a build and displays them on the build page. The actual
 * visualization of the results is defined in the matching
 * <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the results trend.
 * </p>
 * 
 * @param <T>
 *            type of the result of this action
 * @author Ulli Hafner
 */
public abstract class AbstractResultAction<T extends EntriesProvider> 
        implements StaplerProxy, HealthReportingAction, ToolTipProvider,
        ResultAction<T>
{
    /** Unique identifier of this class. */
    private static final long         serialVersionUID = -7201451538713818948L;
    /** Width of the graph. */
    private static final int          WIDTH            = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private final AbstractBuild<?, ?> owner;
    /** Builds a health report. */
    private HealthReportBuilder       healthReportBuilder;
    /** The actual result of this action. */
    private T                         result;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     * 
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     */
    public AbstractResultAction(final AbstractBuild<?, ?> owner,
            final HealthReportBuilder healthReportBuilder)
    {
        super();
        this.owner = owner;
        this.healthReportBuilder = healthReportBuilder;
    }

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     * 
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     * @param result
     *            the result of the action
     */
    public AbstractResultAction(final AbstractBuild<?, ?> owner,
            final HealthReportBuilder healthReportBuilder, final T result)
    {
        this(owner, healthReportBuilder);
        this.result = result;
    }

    /**
     * Returns the data set that represents the result. For each build,
     * the number of warnings is used as result value.
     * 
     * @param useHealthBuilder
     *            determines whether the health builder should be used
     *            to create the data set
     * @return the data set
     */
    protected CategoryDataset buildDataSet(final boolean useHealthBuilder)
    {

        final DataSetBuilder<Integer, NumberOnlyBuildLabel> builder =
                new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (AbstractResultAction<T> action = this; action != null;
        action = action.getPreviousBuild())
        {
            final T current = action.getResult();
            if (current != null)
            {
                List<Integer> series;
                
                    series = new ArrayList<Integer>();
                    System.out.println("getNumberOfSeverityFormatage = "+current.getNumberOfSeverityFormatage());
                    series.add(current.getNumberOfSeverityFormatage());
                    System.out.println("getNumberOfSeverityPerformance = "+current.getNumberOfSeverityPerformance());
                    series.add(current.getNumberOfSeverityPerformance());
                    System.out.println("getNumberOfSeverityDesign = "+current.getNumberOfSeverityDesign());
                    series.add(current.getNumberOfSeverityDesign());
                    System.out.println("getNumberOfSeverityLowSecurity = "+current.getNumberOfSeverityLowSecurity());
                    series.add(current.getNumberOfSeverityLowSecurity());
                    System.out.println("getNumberOfSeverityHighSecurity = "+current.getNumberOfSeverityHighSecurity());
                    series.add(current.getNumberOfSeverityHighSecurity());
                
                int level = 0;
                for (final Integer integer : series)
                {
                    builder.add(integer, level,
                            new NumberOnlyBuildLabel(action.getOwner()));
                    level++;
                }
            }
        }
        return builder.build();
    }
    protected CategoryDataset buildDetailsDataSet(final boolean useHealthBuilder, String param)
    {

        final DataSetBuilder<Integer, NumberOnlyBuildLabel> builder =
                new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (AbstractResultAction<T> action = this; action != null;
        action = action.getPreviousBuild())
        {
            final T current = action.getResult();
            if (current != null)
            {
                List<Integer> series;
                
                series = new ArrayList<Integer>();
                if (param.equals("testedRules"))
                {
                    series.add(current.getNumberOfRules());
                }
                else if (param.equals("errors"))
                {
                    series.add(current.getNumberOfEntry());
                }
                else if (param.equals("newErrors"))
                {
                    series.add(current.getNumberOfEntry());
                }
                else if (param.equals("fixedErrors"))
                {
                    series.add(current.getNumberOfFixedEntry());
                }
                else if (param.equals("unfixedErrors"))
                {
                    series.add(current.getNumberOfNotFixedEntry());
                }
                else if (param.equals("patterns"))
                {
                    series.add(current.getNumberOfPointeurs());
                }
                int level = 0;
                for (final Integer integer : series)
                {
                    builder.add(integer, level,
                            new NumberOnlyBuildLabel(action.getOwner()));
                    level++;
                }
            }
        }
        return builder.build();
    }


    /**
     * Creates the chart for this action.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the chart for this action.
     */
    private JFreeChart createChart(final StaplerRequest request,
            final StaplerResponse response)
    {
        System.out.println("---createChart------");
        final String parameter = request.getParameter("useHealthBuilder");
        final boolean useHealthBuilder =
                Boolean.valueOf(StringUtils.defaultIfEmpty(parameter, "true"));

        return getHealthReportBuilder().createGraph(useHealthBuilder,
                getDescriptor().getPluginResultUrlName(),
                buildDataSet(useHealthBuilder), this);
    }
    /**
     * Creates the chart for this action.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the chart for this action.
     */
    private JFreeChart createDetailsChart(final StaplerRequest request,
            final StaplerResponse response, String param)
    {
        System.out.println("---createRepositoryChart------");
        return getHealthReportBuilder().createGraph(false,
                getDescriptor().getPluginResultUrlName(),
                buildDetailsDataSet(false, param), this);
    }
            

    /**
     * Generates a PNG image for the result trend.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param height
     *            the height of the trend graph
     * @throws IOException
     *             in case of an error
     */
    public final void doGraph(final StaplerRequest request,
            final StaplerResponse response, final int height) throws IOException
    {
        System.out.println("---doGraph------");
        if (ChartUtil.awtProblem)
        {
            response.sendRedirect2(request.getContextPath()
                    + "/images/headless.png");
            return;
        }
        ChartUtil.generateGraph(request, response,
                createChart(request, response), WIDTH, height);
    }
    /**
     * Generates a PNG image for the repository trend
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doDetailsGraph(StaplerRequest request, StaplerResponse response, String param) throws IOException
    {
        System.out.println("---doRepositoryGraph------");
        if (ChartUtil.awtProblem)
        {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartUtil.generateGraph(request, response, createDetailsChart(request, response, param), 850, 200);
    }
    

    /**
     * Generates a PNG image for the result trend.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param height
     *            the height of the trend graph
     * @throws IOException
     *             in case of an error
     */
    public final void doGraphMap(final StaplerRequest request,
            final StaplerResponse response, final int height) throws IOException
    {
        ChartUtil.generateClickableMap(request, response,
                createChart(request, response), WIDTH, height);
    }

    /** {@inheritDoc} */
    public final HealthReport getBuildHealth()
    {
        return healthReportBuilder
                .computeHealth(getResult().getNumberOfPointeurs()
                - getPreviousBuild().getResult().getNumberOfPointeurs());
    }
    /**
     * Returns the descriptor of the associated plug-in.
     * 
     * @return the descriptor of the associated plug-in
     */
    protected abstract SerenitecDescriptor getDescriptor();

    /**
     * Returns the associated health report builder.
     * 
     * @return the associated health report builder
     */
    public final HealthReportBuilder getHealthReportBuilder()
    {

        if (healthReportBuilder == null)
        {
            healthReportBuilder = new HealthReportBuilder();
        }
        return healthReportBuilder;
    }

    /** {@inheritDoc} */
    public String getIconFileName()
    {
        String resultat = null;
        if (getResult().getNumberOfEntry() > 0)
        {
            resultat = getDescriptor().getIconUrl();
        }
        return resultat;
    }

    /**
     * Returns the associated build of this action.
     * 
     * @return the associated build of this action
     */
    public final AbstractBuild<?, ?> getOwner()
    {
        return owner;
    }

    /**
     * Gets the result of a previous build if it's recorded, or <code>null</code> if not.
     * 
     * @return the result of a previous build, or <code>null</code>
     */
    @java.lang.SuppressWarnings("unchecked")
    protected AbstractResultAction<T> getPreviousBuild()
    {
        AbstractBuild<?, ?> build = getOwner();
        while (true)
        {
            build = build.getPreviousBuild();
            if (build == null)
            {
                return null;
            }
            final AbstractResultAction<T> action = build.getAction(getClass());
            if (action != null)
            {
                return action;
            }
        }
    }

    /** {@inheritDoc} */
    public final T getResult()
    {
        return result;
    }

    /** {@inheritDoc} */
    public final Object getTarget()
    {
        return getResult();
    }
    /** {@inheritDoc} */
    public String getUrlName()
    {
        return getDescriptor().getPluginResultUrlName();
    }
    /** {@inheritDoc} */
    public boolean hasPreviousResultAction()
    {
        return getPreviousBuild() != null;
    }

    /** {@inheritDoc} */
    public final void setResult(final T result)
    {
        this.result = result;
    }
}
