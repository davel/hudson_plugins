package hudson.plugins.tasks.util;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.tasks.util.model.AbstractAnnotation;
import hudson.plugins.tasks.util.model.Priority;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Controls the live cycle of Hudson results. This action persists the results
 * of a build and displays them on the build page. The actual visualization of
 * the results is defined in the matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the results trend.
 * </p>
 *
 * @param <T>
 *            type of the result of this action
 * @author Ulli Hafner
 */
public abstract class AbstractResultAction<T extends BuildResult> implements StaplerProxy, HealthReportingAction, ToolTipProvider, ResultAction<T> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -7201451538713818948L;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private final AbstractBuild<?, ?> owner;
    /** Backward compatibility. */
    @Deprecated
    @java.lang.SuppressWarnings("unused")
    private HealthReportBuilder healthReportBuilder;
    /** Parameters for the health report. */
    private final AbstractHealthDescriptor healthDescriptor;
    /** The actual result of this action. */
    private T result;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor
     * @param result
     *            the result of the action
     */
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final AbstractHealthDescriptor healthDescriptor, final T result) {
        this.owner = owner;
        this.result = result;
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor
     */
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final AbstractHealthDescriptor healthDescriptor) {
        this.owner = owner;
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Returns the healthDescriptor.
     *
     * @return the healthDescriptor
     */
    public AbstractHealthDescriptor getHealthDescriptor() {
        if (healthDescriptor != null) {
            return healthDescriptor;
        }
        else {
            return NullHealthDescriptor.NULL_HEALTH_DESCRIPTOR; // for old serialized actions
        }
    }

    /**
     * Returns the descriptor of the associated plug-in.
     *
     * @return the descriptor of the associated plug-in
     */
    protected abstract PluginDescriptor getDescriptor();

    /** {@inheritDoc} */
    public String getUrlName() {
        return getDescriptor().getPluginResultUrlName();
    }

    /**
     * Returns the associated health report builder.
     *
     * @return the associated health report builder
     */
    public final HealthReportBuilder getHealthReportBuilder() {
        return new HealthReportBuilder(getHealthDescriptor());
    }

    /** {@inheritDoc} */
    public final HealthReport getBuildHealth() {
        return getHealthReportBuilder().computeHealth(getResult());
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public final Object getTarget() {
        return getResult();
    }

    /** {@inheritDoc} */
    public final T getResult() {
        return result;
    }

    /** {@inheritDoc} */
    public final void setResult(final T result) {
        this.result = result;
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        if (getResult().getNumberOfAnnotations() > 0) {
            return getDescriptor().getIconUrl();
        }
        return null;
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
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(request), WIDTH, height);
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
        ChartUtil.generateClickableMap(request, response, createChart(request), WIDTH, height);
    }

    /**
     * Creates the chart for this action.
     *
     * @param request
     *            Stapler request
     * @return the chart for this action.
     */
    private JFreeChart createChart(final StaplerRequest request) {
        String parameter = request.getParameter("useHealthBuilder");
        boolean useHealthBuilder = Boolean.valueOf(StringUtils.defaultIfEmpty(parameter, "true"));

        return getHealthReportBuilder().createGraph(useHealthBuilder,
                getDescriptor().getPluginResultUrlName(), buildDataSet(useHealthBuilder), this);
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of warnings is used as result value.
     *
     * @param useHealthBuilder
     *            determines whether the health builder should be used to create
     *            the data set
     * @return the data set
     */
    protected CategoryDataset buildDataSet(final boolean useHealthBuilder) {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (AbstractResultAction<T> action = this; action != null; action = action.getPreviousBuild()) {
            T current = action.getResult();
            if (current != null) {
                List<Integer> series;
                if (useHealthBuilder && getHealthReportBuilder().isEnabled()) {
                    series = getHealthReportBuilder().createSeries(current.getNumberOfAnnotations());
                }
                else {
                    series = new ArrayList<Integer>();
                    series.add(current.getNumberOfAnnotations(Priority.LOW));
                    series.add(current.getNumberOfAnnotations(Priority.NORMAL));
                    series.add(current.getNumberOfAnnotations(Priority.HIGH));
                }
                int level = 0;
                for (Integer integer : series) {
                    builder.add(integer, level, new NumberOnlyBuildLabel(action.getOwner()));
                    level++;
                }
            }
        }
        return builder.build();
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
            AbstractResultAction<T> action = build.getAction(getClass());
            if (action != null) {
                return action;
            }
        }
    }

    /** {@inheritDoc} */
    public boolean hasPreviousResultAction() {
        return getPreviousBuild() != null;
    }

    /**
     * Aggregates the results of the specified maven module builds.
     *
     * @param moduleBuilds
     *            the module builds to aggregate
     * @return the aggregated result
     */
    protected ParserResult createAggregatedResult(final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        ParserResult project = createResult();
        for (List<MavenBuild> builds : moduleBuilds.values()) {
            if (!builds.isEmpty()) {
                addModule(project, builds);
            }
        }
        return project;
    }

    /**
     * Factory method to create the result of this action.
     *
     * @return the result of this action
     */
    protected ParserResult createResult() {
        return new ParserResult();
    }

    /**
     * Adds a new module to the specified project. The new module is obtained
     * from the specified list of builds.
     *
     * @param aggregatedResult
     *            the result to add the module to
     * @param builds
     *            the builds for a module
     */
    // FIXME: this method is always invoked with all available builds, check this for hierarchies
    @java.lang.SuppressWarnings("unchecked")
    protected void addModule(final ParserResult aggregatedResult, final List<MavenBuild> builds) {
        MavenBuild mavenBuild = builds.get(0);
        AbstractResultAction<T> action = mavenBuild.getAction(getClass());
        if (action != null) {
            aggregatedResult.addAnnotations(action.getResult().getAnnotations());
            aggregatedResult.addModules(action.getResult().getModules());
            FilePath filePath = new FilePath(new File(mavenBuild.getRootDir(), AbstractAnnotation.WORKSPACE_FILES));
            try {
                filePath.copyRecursiveTo("*.tmp", new FilePath(new File(getOwner().getRootDir(), AbstractAnnotation.WORKSPACE_FILES)));
            }
            catch (IOException exception) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Can't copy workspace files: ", exception);
            }
            catch (InterruptedException exception) {
                // ignore, user canceled the operation
            }
        }
    }
}
