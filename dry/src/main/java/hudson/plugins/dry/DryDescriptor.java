package hudson.plugins.dry;

import hudson.Extension;
import hudson.plugins.analysis.core.PluginDescriptor;


/**
 * Descriptor for the class {@link DryPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
@Extension(ordinal = 100)
public final class DryDescriptor extends PluginDescriptor {
    /** Plug-in name. */
    private static final String PLUGIN_NAME = "dry";
    /** Icon to use for the result and project action. */
    private static final String ACTION_ICON = "/plugin/dry/icons/dry-24x24.png";

    /**
     * Instantiates a new find bugs descriptor.
     */
    public DryDescriptor() {
        super(DryPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.DRY_Publisher_Name();
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
}