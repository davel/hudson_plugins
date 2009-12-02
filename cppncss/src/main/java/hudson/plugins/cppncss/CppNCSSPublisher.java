package hudson.plugins.cppncss;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.plugins.helpers.AbstractPublisherImpl;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.helpers.health.HealthMetric;
import hudson.tasks.BuildStepDescriptor;
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
public class CppNCSSPublisher extends AbstractPublisherImpl {

    private String reportFilenamePattern;
    private Integer functionCcnViolationThreshold = 10;
    private Integer functionNcssViolationThreshold = 100;
    private CppNCSSHealthTarget[] targets;

    @DataBoundConstructor
    public CppNCSSPublisher(String reportFilenamePattern, Integer functionCcnViolationThreshold, Integer functionNcssViolationThreshold, CppNCSSHealthTarget[] targets) {
		reportFilenamePattern.getClass();
        this.reportFilenamePattern = reportFilenamePattern;
        this.functionCcnViolationThreshold = functionCcnViolationThreshold;
        this.functionNcssViolationThreshold = functionNcssViolationThreshold;
        
        this.targets = targets == null ? new CppNCSSHealthTarget[0] : targets;
    }

    public String getReportFilenamePattern() {
        return reportFilenamePattern;
    }

	public Integer getFunctionCcnViolationThreshold() {
		return functionCcnViolationThreshold;
	}

	public Integer getFunctionNcssViolationThreshold() {
		return functionNcssViolationThreshold;
	}

	public CppNCSSHealthTarget[] getTargets() {
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsToRunAfterFinalized() {
        return false;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * {@inheritDoc}
     */
    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * {@inheritDoc}
     */
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new CppNCSSProjectIndividualReport(project, functionCcnViolationThreshold, functionNcssViolationThreshold);
    }

    protected Ghostwriter newGhostwriter() {
        return new CppNCSSGhostwriter(reportFilenamePattern, functionCcnViolationThreshold, functionNcssViolationThreshold, targets);
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Do not instantiate DescriptorImpl.
         */
        private DescriptorImpl() {
            super(CppNCSSPublisher.class);
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ConvertUtils.register(CppNCSSHealthMetrics.CONVERTER, CppNCSSHealthMetrics.class);
            return req.bindJSON(CppNCSSPublisher.class, formData);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return FreeStyleProject.class.isAssignableFrom(aClass);
        }

        public HealthMetric[] getMetrics() {
            return CppNCSSHealthMetrics.values();
        }
    }

}
