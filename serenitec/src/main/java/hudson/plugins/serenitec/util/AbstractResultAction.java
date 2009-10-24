/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/24 09:44:14 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;


import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.serenitec.SerenitecDescriptor;
import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.plugins.serenitec.util.model.EntriesProvider;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Controls the live cycle of Hudson results. This action persists the results of a build and displays them on the build page. The actual
 * visualization of the results is defined in the matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the results trend.
 * </p>
 * 
 * @param <T>
 *            type of the result of this action
 * @author Ulli Hafner
 */
public abstract class AbstractResultAction<T extends EntriesProvider> implements StaplerProxy, HealthReportingAction, ToolTipProvider,
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
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final HealthReportBuilder healthReportBuilder) {

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
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final HealthReportBuilder healthReportBuilder, final T result) {

        this(owner, healthReportBuilder);
        this.result = result;
    }

    /**
     * Returns the data set that represents the result. For each build, the number of warnings is used as result value.
     * 
     * @param useHealthBuilder
     *            determines whether the health builder should be used to create the data set
     * @return the data set
     */
    protected CategoryDataset buildDataSet(final boolean useHealthBuilder) {

        final DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (AbstractResultAction<T> action = this; action != null; action = action.getPreviousBuild()) {
            final T current = action.getResult();
            if (current != null) {
                List<Integer> series;

                series = new ArrayList<Integer>();
                series.add(current.getNumberOfSeverityFormatage());
                series.add(current.getNumberOfSeverityPerformance());
                series.add(current.getNumberOfSeverityDesign());
                series.add(current.getNumberOfSeverityLowSecurity());
                series.add(current.getNumberOfSeverityHighSecurity());

                int level = 0;
                for (final Integer integer : series) {
                    builder.add(integer, level, new NumberOnlyBuildLabel(action.getOwner()));
                    level++;
                }
            }
        }
        return builder.build();
    }
    protected CategoryDataset buildDetailsDataSet(final boolean useHealthBuilder, String param) {

        final DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (AbstractResultAction<T> action = this; action != null; action = action.getPreviousBuild()) {
            final T current = action.getResult();
            if (current != null) {
                List<Integer> series;

                series = new ArrayList<Integer>();
                if (param.equals("testedRules")) {
                    series.add(current.getNumberOfRules());
                } else if (param.equals("errors")) {
                    series.add(current.getNumberOfEntry());
                } else if (param.equals("newErrors")) {
                    series.add(current.getNumberOfNewEntry());
                } else if (param.equals("fixedErrors")) {
                    series.add(current.getNumberOfFixedEntry());
                } else if (param.equals("unfixedErrors")) {
                    series.add(current.getNumberOfNotFixedEntry());
                } else if (param.equals("patterns")) {
                    series.add(current.getNumberOfPointeurs());
                }
                int level = 0;
                for (final Integer integer : series) {
                    builder.add(integer, level, new NumberOnlyBuildLabel(action.getOwner()));
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
    private JFreeChart createChart(final StaplerRequest request, final StaplerResponse response) {

        System.out.println("---createChart------");
        final String parameter = request.getParameter("useHealthBuilder");
        final boolean useHealthBuilder = Boolean.valueOf(StringUtils.defaultIfEmpty(parameter, "true"));

        return getHealthReportBuilder().createGraph(useHealthBuilder, getDescriptor().getPluginResultUrlName(),
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
    private JFreeChart createRulesRepartitionChart(final StaplerRequest request, final StaplerResponse response, final int n1,
            final int n2, final String titre) {

        JFreeChart chart = null;

        DefaultPieDataset pieDataset = new DefaultPieDataset();
        pieDataset.setValue(titre + "(" + n1 * 100.0f / n2 + "%)", n1);
        pieDataset.setValue("Remains", (n2 - n1));
        chart = ChartFactory.createPieChart3D(null, pieDataset, false, true, false);
        PiePlot3D p = (PiePlot3D) chart.getPlot();
        p.setForegroundAlpha(0.5f);
        return chart;

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
    private JFreeChart createPersonalChart(final StaplerRequest request, final StaplerResponse response, final String type) {

        JFreeChart chart = null;
        if (type.equals("errorBySeverityTrend")) {
            System.out.println("errorBySeverityTrend");

            DefaultPieDataset pieDataset = new DefaultPieDataset();
            pieDataset.setValue("Formating", getResult().getNumberOfSeverityFormatage());
            pieDataset.setValue("Language evolution", getResult().getNumberOfSeverityPerformance());
            pieDataset.setValue("Design", getResult().getNumberOfSeverityDesign());
            pieDataset.setValue("Low Security", getResult().getNumberOfSeverityLowSecurity());
            pieDataset.setValue("High Security", getResult().getNumberOfSeverityHighSecurity());
            chart = ChartFactory.createPieChart3D("Errors by Severity", pieDataset, false, true, false);
            PiePlot3D p = (PiePlot3D) chart.getPlot();
            p.setForegroundAlpha(0.5f);
        } else if (type.equals("patternsBySeverityTrend")) {
            System.out.println("patternsBySeverityTrend");

            DefaultPieDataset pieDataset = new DefaultPieDataset();
            pieDataset.setValue("Formating", getResult().getNumberOfSeverityFormatagePatterns());
            pieDataset.setValue("Language evolution", getResult().getNumberOfSeverityPerformancePatterns());
            pieDataset.setValue("Design", getResult().getNumberOfSeverityDesignPatterns());
            pieDataset.setValue("Low Security", getResult().getNumberOfSeverityLowSecurityPatterns());
            pieDataset.setValue("High Security", getResult().getNumberOfSeverityHighSecurityPatterns());
            chart = ChartFactory.createPieChart3D("Patterns by Severity", pieDataset, false, true, false);
            PiePlot3D p = (PiePlot3D) chart.getPlot();
            p.setForegroundAlpha(0.5f);
        } else if (type.equals("topFiveTrend")) {
            System.out.println("topFiveTrend");
            DefaultCategoryDataset categorieDataset = new DefaultCategoryDataset();
            int numberOfPatternsForTopFive = 0;
            int numberOfPatternsForNotTopFive = 0;
            for (ReportEntry topfiveEntry : getResult().getTopFiveEntries()) {
                numberOfPatternsForTopFive += topfiveEntry.getNumberOfPointeurs();
            }
            numberOfPatternsForNotTopFive = getResult().getNumberOfPointeurs() - numberOfPatternsForTopFive;

            categorieDataset.addValue(numberOfPatternsForTopFive, "TopFive (" + numberOfPatternsForTopFive * 100.0f
                    / getResult().getNumberOfPointeurs() + "%)", "Patterns");
            categorieDataset.addValue(numberOfPatternsForNotTopFive, "Remains (" + numberOfPatternsForNotTopFive * 100.0f
                    / getResult().getNumberOfPointeurs() + "%)", "Patterns");

            chart = ChartFactory.createStackedBarChart3D(null, // chart title
                    null, // domain axis label
                    null, // range axis label
                    categorieDataset, // data
                    PlotOrientation.HORIZONTAL, // the plot orientation
                    true, // include legend
                    true, // tooltips
                    false // urls
                    );
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();
            final CategoryItemRenderer renderer = plot.getRenderer();
            plot.setForegroundAlpha(0.5f);

            return chart;
        } else if (type.equals("coverage")) {
            System.out.println("Coverage");
            DefaultCategoryDataset categorieDataset = new DefaultCategoryDataset();
            int numberOfFilesWithErrors = 0;
            int numberOfFilesWithNoErrors = 0;
            numberOfFilesWithErrors = getResult().getNumberOfFilesWithErrors();
            numberOfFilesWithNoErrors = getResult().getNumberOfFilesWithNoErrors();

            categorieDataset.addValue(numberOfFilesWithErrors, "Files with Errors (" + numberOfFilesWithErrors * 100.0f
                    / getResult().getNumberOfFiles() + "%)", "Files");
            categorieDataset.addValue(numberOfFilesWithNoErrors, "Remains (" + numberOfFilesWithNoErrors * 100.0f
                    / getResult().getNumberOfFiles() + "%)", "Files");

            chart = ChartFactory.createStackedBarChart3D(null, // chart title
                    null, // domain axis label
                    null, // range axis label
                    categorieDataset, // data
                    PlotOrientation.HORIZONTAL, // the plot orientation
                    true, // include legend
                    true, // tooltips
                    false // urls
                    );
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();
            final CategoryItemRenderer renderer = plot.getRenderer();
            plot.setForegroundAlpha(0.5f);

            return chart;
        }

        return chart;

        /**
         * return getHealthReportBuilder().createPersonalGraph( getDescriptor().getPluginResultUrlName(), buildPersonalDataSet(type), this);
         */
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
    private JFreeChart createDetailsChart(final StaplerRequest request, final StaplerResponse response, String param) {

        System.out.println("---createRepositoryChart------");
        return getHealthReportBuilder().createGraph(false, getDescriptor().getPluginResultUrlName(), buildDetailsDataSet(false, param),
                this);
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
    public final void doGraph(final StaplerRequest request, final StaplerResponse response, final int height) throws IOException {

        System.out.println("---doGraph------");
        if (ChartUtil.awtProblemCause != null) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(request, response), WIDTH, height);
    }

    /**
     * Generates a PNG image for the result personal trend.
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
    public final void doPersonalGraph(final StaplerRequest request, final StaplerResponse response, int height, final String type)
            throws IOException {

        System.out.println("---doGraph------");
        if (ChartUtil.awtProblemCause != null) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        int width = 400;
        if (type.equals("topFiveTrend") || type.equals("coverage")) {
            width = 800;
            height = 100;
        }

        ChartUtil.generateGraph(request, response, createPersonalChart(request, response, type), width, height);
    }
    /**
     * Generates a PNG image for the result personal trend.
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
    public final void doRulesRepartitionPie(final StaplerRequest request, final StaplerResponse response, int size, final int n1,
            final int n2, final String titre) throws IOException {

        System.out.println("---doGraph------");
        if (ChartUtil.awtProblemCause != null) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartUtil.generateGraph(request, response, createRulesRepartitionChart(request, response, n1, n2, titre), 300, 150);
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
    public void doDetailsGraph(StaplerRequest request, StaplerResponse response, String param) throws IOException {

        System.out.println("---doRepositoryGraph------");
        if (ChartUtil.awtProblemCause != null) {
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
    public final void doGraphMap(final StaplerRequest request, final StaplerResponse response, final int height) throws IOException {

        ChartUtil.generateClickableMap(request, response, createChart(request, response), WIDTH, height);
    }

    /** {@inheritDoc} */
    public final HealthReport getBuildHealth() {

        return healthReportBuilder
                .computeHealth(getResult().getNumberOfPointeurs() - getPreviousBuild().getResult().getNumberOfPointeurs());
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
    public final HealthReportBuilder getHealthReportBuilder() {

        if (healthReportBuilder == null) {
            healthReportBuilder = new HealthReportBuilder();
        }
        return healthReportBuilder;
    }

    /** {@inheritDoc} */
    public String getIconFileName() {

        String resultat = null;
        if (getResult().getNumberOfEntry() > 0) {
            resultat = getDescriptor().getIconUrl();
        }
        return resultat;
    }

    /**
     * Returns the associated build of this action.
     * 
     * @return the associated build of this action
     */
    public final AbstractBuild<?, ?> getOwner() {

        return owner;
    }

    /**
     * Gets the result of a previous build if it's recorded, or <code>null</code> if not.
     * 
     * @return the result of a previous build, or <code>null</code>
     */
    @java.lang.SuppressWarnings("unchecked")
    protected AbstractResultAction<T> getPreviousBuild() {

        AbstractBuild<?, ?> build = getOwner();
        while (true) {
            build = build.getPreviousBuild();
            if (build == null) {
                return null;
            }
            final AbstractResultAction<T> action = build.getAction(getClass());
            if (action != null) {
                return action;
            }
        }
    }

    /** {@inheritDoc} */
    public final T getResult() {

        return result;
    }

    /** {@inheritDoc} */
    public final Object getTarget() {

        return getResult();
    }
    /** {@inheritDoc} */
    public String getUrlName() {

        return getDescriptor().getPluginResultUrlName();
    }
    /** {@inheritDoc} */
    public boolean hasPreviousResultAction() {

        return getPreviousBuild() != null;
    }

    /** {@inheritDoc} */
    public final void setResult(final T result) {

        this.result = result;
    }
}
