package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;
import hudson.plugins.findbugs.util.ChartBuilder;
import hudson.util.ChartUtil;
import hudson.util.IOException2;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the FindBugs analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class FindBugsResult implements ModelObject, Serializable {
    /** No result at all. */
    @java.lang.SuppressWarnings("unchecked")
    private static final Set<Warning> EMPTY_SET = Collections.EMPTY_SET;
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2768250056765266658L;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(FindBugsResult.class.getName());
    /** Difference between this and the previous build. */
    private final int delta;
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The parsed FindBugs result. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** All new warnings in the current build.*/
    @SuppressWarnings("Se")
    private transient WeakReference<Set<Warning>> newWarnings;
    /** All fixed warnings in the current build.*/
    @SuppressWarnings("Se")
    private transient WeakReference<Set<Warning>> fixedWarnings;
    /** The number of warnings in this build. */
    private final int numberOfWarnings;
    /** The number of new warnings in this build. */
    private final int numberOfNewWarnings;
    /** The number of fixed warnings in this build. */
    private final int numberOfFixedWarnings;
    /** The number of low priority warnings in this build. */
    private int low;
    /** The number of normal priority warnings in this build. */
    private int normal;
    /** The number of high priority warnings in this build. */
    private int high;

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     */
    public FindBugsResult(final Build<?, ?> build, final JavaProject project) {
        this(build, project, new JavaProject());
    }

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     * @param previousProject the parsed FindBugs result of the previous build
     */
    public FindBugsResult(final Build<?, ?> build, final JavaProject project, final JavaProject previousProject) {
        owner = build;
        numberOfWarnings = project.getNumberOfWarnings();

        this.project = new WeakReference<JavaProject>(project);
        delta = project.getNumberOfWarnings() - previousProject.getNumberOfWarnings();

        Set<Warning> allWarnings = project.getWarnings();

        Set<Warning> warnings = WarningDifferencer.getNewWarnings(allWarnings, previousProject.getWarnings());
        numberOfNewWarnings = warnings.size();
        newWarnings = new WeakReference<Set<Warning>>(warnings);

        warnings = WarningDifferencer.getFixedWarnings(allWarnings, previousProject.getWarnings());
        numberOfFixedWarnings = warnings.size();
        fixedWarnings = new WeakReference<Set<Warning>>(warnings);

        computePriorities(allWarnings);
    }

    /**
     * Computes the low, normal and high priority count.
     *
     * @param allWarnings
     *            all project warnings
     */
    private void computePriorities(final Set<Warning> allWarnings) {
        low = WarningDifferencer.countLowPriorityWarnings(allWarnings);
        normal = WarningDifferencer.countNormalPriorityWarnings(allWarnings);
        high = WarningDifferencer.countHighPriorityWarnings(allWarnings);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Result";
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Gets the number of warnings.
     *
     * @return the number of warnings
     */
    public int getNumberOfWarnings() {
        return numberOfWarnings;
    }

    /**
     * Gets the number of fixed warnings.
     *
     * @return the number of fixed warnings
     */
    public int getNumberOfFixedWarnings() {
        return numberOfFixedWarnings;
    }

    /**
     * Gets the number of new warnings.
     *
     * @return the number of new warnings
     */
    public int getNumberOfNewWarnings() {
        return numberOfNewWarnings;
    }

    /**
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public JavaProject getProject() {
        try {
            if (project == null) {
                loadResult();
            }
            JavaProject result = project.get();
            if (result == null) {
                loadResult();
            }
            return project.get();
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files.", exception);
        }
        catch (InterruptedException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files: operation has been canceled.", exception);
        }
        return new JavaProject();
    }

    /**
     * Returns the new warnings of this build.
     *
     * @return the new warnings of this build.
     */
    public Set<Warning> getNewWarnings() {
        try {
            if (newWarnings == null) {
                loadResult();
            }
            Set<Warning> result = newWarnings.get();
            if (result == null) {
                loadResult();
            }
            return newWarnings.get();
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files.", exception);
        }
        catch (InterruptedException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files: operation has been canceled.", exception);
        }
        return EMPTY_SET;
    }

    /**
     * Returns the fixed warnings of this build.
     *
     * @return the fixed warnings of this build.
     */
    public Set<Warning> getFixedWarnings() {
        try {
            if (fixedWarnings == null) {
                loadResult();
            }
            Set<Warning> result = fixedWarnings.get();
            if (result == null) {
                loadResult();
            }
            return fixedWarnings.get();
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files.", exception);
        }
        catch (InterruptedException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files: operation has been canceled.", exception);
        }
        return EMPTY_SET;
    }

    /**
     * Loads the FindBugs results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     *
     * @throws IOException if the files could not be read
     * @throws InterruptedException if the operation has been canceled
     */
    private void loadResult() throws IOException, InterruptedException {
        try {
            FindBugsCounter findBugsCounter = new FindBugsCounter(owner);
            JavaProject result = findBugsCounter.findBugs();
            if (isCurrent() && result.isMavenFormat()) {
                findBugsCounter.restoreMapping(result);
            }
            computePriorities(result.getWarnings());

            project = new WeakReference<JavaProject>(result);

            if (hasPreviousResult()) {
                newWarnings = new WeakReference<Set<Warning>>(
                        WarningDifferencer.getNewWarnings(result.getWarnings(), getPreviousResult().getWarnings()));
            }
            else {
                newWarnings = new WeakReference<Set<Warning>>(result.getWarnings());
            }
            if (hasPreviousResult()) {
                fixedWarnings = new WeakReference<Set<Warning>>(
                        WarningDifferencer.getFixedWarnings(result.getWarnings(), getPreviousResult().getWarnings()));
            }
            else {
                fixedWarnings = new WeakReference<Set<Warning>>(EMPTY_SET);
            }
        }
        catch (SAXException exception) {
            throw new IOException2(exception);
        }
    }

    /**
     * Returns the dynamic result of the FindBugs analysis (detail page for a package).
     *
     * @param packageName the package name to get the result for
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a package).
     */
    public Object getDynamic(final String packageName, final StaplerRequest request, final StaplerResponse response) {
        return new FindBugsDetail(owner, getProject(), packageName);
    }

    /**
     * Returns the number of warnings of the specified package in the previous build.
     *
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getPreviousNumberOfWarnings(final String packageName) {
        FindBugsResultAction action = owner.getAction(FindBugsResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject().getNumberOfWarnings(packageName);
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    public JavaProject getPreviousResult() {
        FindBugsResultAction action = owner.getAction(FindBugsResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    public boolean hasPreviousResult() {
        return owner.getAction(FindBugsResultAction.class).hasPreviousResultAction();
    }

    /**
     * Returns the total number of warnings with priority LOW in this package.
     *
     * @return the total number of warnings with priority LOW in this package
     */
    public int getNumberOfLowWarnings() {
        return low;
    }

    /**
     * Returns the total number of warnings with priority HIGH in this package.
     *
     * @return the total number of warnings with priority HIGH in this package
     */
    public int getNumberOfHighWarnings() {
        return high;
    }

    /**
     * Returns the total number of warnings with priority NORMAL in this package.
     *
     * @return the total number of warnings with priority NORMAL in this package
     */
    public int getNumberOfNormalWarnings() {
        return normal;
    }

    /**
     * Generates a PNG image for high/normal/low distribution of a maven module.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doModuleStatistics(final StaplerRequest request,
            final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        Module module = getProject().getModule(request.getParameter("module"));

        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createHighNormalLowChart(
                module.getNumberOfHighWarnings(),
                module.getNumberOfNormalWarnings(),
                module.getNumberOfLowWarnings(), getProject().getWarningBound());
        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }
}
