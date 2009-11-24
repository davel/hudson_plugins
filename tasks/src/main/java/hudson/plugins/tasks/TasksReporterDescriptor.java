package hudson.plugins.tasks;

import hudson.maven.MavenReporter;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.core.ReporterDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link TasksReporter}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public class TasksReporterDescriptor extends ReporterDescriptor {
    /**
     * Creates a new instance of <code>TasksReporterDescriptor</code>.
     *
     * @param pluginDescriptor
     *            the plug-in descriptor of the publisher
     */
    public TasksReporterDescriptor(final PluginDescriptor pluginDescriptor) {
        super(TasksReporter.class, pluginDescriptor);
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporter newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        return request.bindJSON(TasksReporter.class, formData);
    }
}

