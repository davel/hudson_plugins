package hudson.plugins.analysis.collector;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.checkstyle.CheckStyleResultAction;
import hudson.plugins.dry.DryResultAction;
import hudson.plugins.findbugs.FindBugsResultAction;
import hudson.plugins.pmd.PmdResultAction;
import hudson.plugins.tasks.TasksResultAction;
import hudson.plugins.warnings.WarningsResultAction;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Collects the results of the various analysis plug-ins.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class AnalysisPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 5512072640635006098L;

    /** Descriptor of this publisher. */
    @Extension(ordinal = 10)
    public static final AnalysisDescriptor DESCRIPTOR = new AnalysisDescriptor();

    /**
     * Creates a new instance of {@link AnalysisPublisher}.
     *
     * @param threshold
     *            Annotation threshold to be reached if a build should be
     *            considered as unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be
     *            considered as failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of annotations is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of annotations is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public AnalysisPublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, defaultEncoding, "ANALYSIS-COLLECTOR");
    }
    // CHECKSTYLE:ON

    /**
     * Initializes the plug-ins that should participate in the results of this
     * analysis collector.
     *
     * @return the plug-in actions to read the results from
     */
    private ArrayList<Class<? extends AbstractResultAction<? extends BuildResult>>> getParticipatingPlugins() {
        ArrayList<Class<? extends AbstractResultAction<? extends BuildResult>>> pluginResults;
        pluginResults = new ArrayList<Class<? extends AbstractResultAction<? extends BuildResult>>>();

        addPlugin(pluginResults, "checkstyle", CheckStyleResultAction.class);
        addPlugin(pluginResults, "dry", DryResultAction.class);
        addPlugin(pluginResults, "findbugs", FindBugsResultAction.class);
        addPlugin(pluginResults, "pmd", PmdResultAction.class);
        addPlugin(pluginResults, "tasks", TasksResultAction.class);
        addPlugin(pluginResults, "warnings", WarningsResultAction.class);

        return pluginResults;
    }

    /**
     * Adds the specified plug-in to the list of actions.
     *
     * @param pluginResults
     *            the list of actions this plug-in will be added to
     * @param plugin
     *            the plug-in to consider
     * @param action
     *            the action to add
     */
    private void addPlugin(final List<Class<? extends AbstractResultAction<? extends BuildResult>>> pluginResults, final String plugin,
            final Class<? extends AbstractResultAction<? extends BuildResult>> action) {
        if (Hudson.getInstance().getPlugin(plugin) != null) {
            pluginResults.add(action);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new AnalysisProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        ParserResult overallResult = new ParserResult();
        for (Class<? extends AbstractResultAction<? extends BuildResult>> result : getParticipatingPlugins()) {
            AbstractResultAction<? extends BuildResult> action = build.getAction(result);
            if (action != null) {
                BuildResult actualResult = action.getResult();
                Collection<FileAnnotation> annotactualResultations = actualResult.getAnnotations();
                overallResult.addAnnotations(annotactualResultations);
            }
        }

        AnalysisResult result = new AnalysisResultBuilder().build(build, overallResult, getDefaultEncoding());
        build.getActions().add(new AnalysisResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean canContinue(final Result result) {
        return true;
    }
}
