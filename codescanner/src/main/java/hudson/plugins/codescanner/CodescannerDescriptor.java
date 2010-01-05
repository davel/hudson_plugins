package hudson.plugins.codescanner;

import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.analysis.core.PluginDescriptor;

import net.sf.json.JSONObject;

import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link CodescannerPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Maximilian Odendahl
 */
public final class CodescannerDescriptor extends PluginDescriptor {
    /** Plug-in name. */
    private static final String PLUGIN_NAME = "codescanner";
    /** Icon to use for the result and project action. */
    private static final String ACTION_ICON = "/plugin/codescanner/icons/warnings-24x24.png";

    /**
     * Instantiates a new find bugs descriptor.
     */
    CodescannerDescriptor() {
        super(CodescannerPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.Codescanner_Publisher_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getIconUrl() {
        return ACTION_ICON;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        
        return FreeStyleProject.class.isAssignableFrom(jobType);
    }

    /** {@inheritDoc} */
    @Override
    public CodescannerPublisher newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        CodescannerPublisher publisher = request.bindJSON(CodescannerPublisher.class, formData);

        return publisher;
    }
}