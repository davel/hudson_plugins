package hudson.plugins.tasks.util;

import hudson.Util;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. The renderer also displays tooltips for each selected build.
 * <ul>
 * <li>The tooltip is computed per column (i.e., per build) and row (i.e., priority) and shows the
 * number of annotations of the selected priority for this build.</li>
 * <li>The link is also computed per column and links to the results for this
 * build.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
// TODO: the link should be aware of the priorities and filter the selected priority
public final class PrioritiesAreaRenderer extends AbstractAreaRenderer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4683951507836348304L;

    /**
     * Creates a new instance of <code>PrioritiesAreaRenderer</code>.
     *
     * @param url
     *            base URL of the graph links
     * @param singleTooltip
     *            tooltip if there is one item
     * @param multipleTooltip
     *            tooltip if there are multiple items
     */
    public PrioritiesAreaRenderer(final String url, final String singleTooltip, final String multipleTooltip) {
        super(url, singleTooltip, multipleTooltip);
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        StringBuilder tooltip = new StringBuilder();
        int number = dataset.getValue(row, column).intValue();
        if (number == 1) {
            tooltip.append(getSingleTooltip());
        }
        else {
            tooltip.append(getMultipleTooltip(number));
        }
        tooltip.append(" ");
        if (row == 2) {
            tooltip.append(Messages.Trend_PriorityHigh());
        }
        else if (row == 1) {
            tooltip.append(Messages.Trend_PriorityNormal());
        }
        else {
            tooltip.append(Messages.Trend_PriorityLow());
    }
        return tooltip.toString();
    }
}