package hudson.plugins.codescanner;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;

/**
 * Controls the live cycle of the warnings results. This action persists the
 * results of the warnings analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the warnings result trend.
 * </p>
 *
 * @author Maximilian Odendahl
 */
public class CodescannerResultAction extends AbstractResultAction<CodescannerResult> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842874L;

    /**
     * Creates a new instance of <code>CodescannerResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param result
     *            the result in this build
     */
    public CodescannerResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final CodescannerResult result) {
        super(owner, new CodescannerHealthDescriptor(healthDescriptor), result);
    }

    /**
     * Creates a new instance of <code>CodescannerResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     */
    public CodescannerResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor) {
        super(owner, new CodescannerHealthDescriptor(healthDescriptor));
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Codescanner_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected PluginDescriptor getDescriptor() {
        return CodescannerPublisher.CODESCANNER_DESCRIPTOR;
    }

    /** {@inheritDoc} */
    @Override
    public String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.Codescanner_ResultAction_MultipleWarnings(numberOfItems);
    }

    /** {@inheritDoc} */
    @Override
    public String getSingleItemTooltip() {
        return Messages.Codescanner_ResultAction_OneWarning();
    }
}
