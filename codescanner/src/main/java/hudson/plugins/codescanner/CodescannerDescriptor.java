package hudson.plugins.codescanner;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;

import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CodescannerPublisher newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        Set<String> parsers = extractParsers(formData);

        CodescannerPublisher publisher = request.bindJSON(CodescannerPublisher.class, formData);

        return publisher;
    }

    /**
     * Extract the list of parsers to use from the JSON form data.
     *
     * @param formData
     *            the JSON form data
     * @return the list of parsers to use
     */
    private Set<String> extractParsers(final JSONObject formData) {
        Set<String> parsers = new HashSet<String>();
        Object values = formData.get("parsers");
        if (values instanceof JSONArray) {
            JSONArray array = (JSONArray)values;
            for (int i = 0; i < array.size(); i++) {
                JSONObject element = array.getJSONObject(i);
                parsers.add(element.getString("parserName"));
            }
            formData.remove("parsers");
        }
        else if (values instanceof JSONObject) {
            JSONObject object = (JSONObject)values;
            parsers.add(object.getString("parserName"));
            formData.remove("parsers");
        }

        return parsers;
    }
}