package hudson.plugins.coverage;

import java.util.Set;

import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.plugins.helpers.AbstractPublisherImpl;
import hudson.plugins.helpers.Ghostwriter;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


/**
 * Generic code coverage {@link hudson.tasks.Publisher}.
 *
 * @author Stephen Connolly
 * @since 1.0
 */
public class CoveragePublisher extends AbstractPublisherImpl {

    private CoverageHealthTarget[] targets;

    @DataBoundConstructor
    public CoveragePublisher(CoverageHealthTarget... targets) {
        this.targets = targets == null ? new CoverageHealthTarget[0] : targets;
    }

    public CoverageHealthTarget[] getTargets() {
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    public Descriptor<Publisher> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    protected Ghostwriter newGhostwriter() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Descriptor for {@link CoveragePublisher}.
     */
    public static final class DescriptorImpl extends Descriptor<Publisher> {

        /**
         * Constructs a new DescriptorImpl.
         */
        public DescriptorImpl() {
            super(CoveragePublisher.class);
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Record code coverage";
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return !MavenModuleSet.class.isAssignableFrom(aClass)
                    && !MavenModule.class.isAssignableFrom(aClass);
        }

        public Set<CoverageHealthMetrics> getMetrics() {
            return CoverageHealthMetrics.values();
        }
    }
}
