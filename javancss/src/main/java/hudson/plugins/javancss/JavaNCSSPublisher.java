package hudson.plugins.javancss;

import hudson.Extension;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.plugins.helpers.AbstractPublisherImpl;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.helpers.health.HealthMetric;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:24:06
 */
public class JavaNCSSPublisher extends AbstractPublisherImpl {

    private String reportFilenamePattern;
    private JavaNCSSHealthTarget[] targets;

    @DataBoundConstructor
    public JavaNCSSPublisher(String reportFilenamePattern, JavaNCSSHealthTarget[] targets) {
        reportFilenamePattern.getClass();
        this.reportFilenamePattern = reportFilenamePattern;
        this.targets = targets == null ? new JavaNCSSHealthTarget[0] : targets;
    }

    public String getReportFilenamePattern() {
        return reportFilenamePattern;
    }

    public JavaNCSSHealthTarget[] getTargets() {
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsToRunAfterFinalized() {
        return false;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new JavaNCSSProjectIndividualReport(project);
    }

    protected Ghostwriter newGhostwriter() {
        return new JavaNCSSGhostwriter(reportFilenamePattern, targets);
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Do not instantiate DescriptorImpl.
         */
        private DescriptorImpl() {
            super(JavaNCSSPublisher.class);
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ConvertUtils.register(JavaNCSSHealthMetrics.CONVERTER, JavaNCSSHealthMetrics.class);
            return req.bindJSON(JavaNCSSPublisher.class, formData);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return !MavenModuleSet.class.isAssignableFrom(aClass)
                    && !MavenModule.class.isAssignableFrom(aClass);
        }

        public HealthMetric[] getMetrics() {
            return JavaNCSSHealthMetrics.values();
        }
    }

}
