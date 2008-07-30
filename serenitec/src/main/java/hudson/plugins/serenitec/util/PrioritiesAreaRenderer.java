/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:24 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;

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
public class PrioritiesAreaRenderer extends StackedAreaRenderer2
{

    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4683951507836348304L;
    private final String url;
    /** Tooltip provider for the clickable map. */
    private final ToolTipBuilder toolTipBuilder;

    /**
     * Creates a new instance of <code>PrioritiesAreaRenderer</code>.
     *
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     */
    public PrioritiesAreaRenderer(final String url, final ToolTipProvider toolTipProvider)
    {
        super();
        toolTipBuilder = new ToolTipBuilder(toolTipProvider);
        this.url = "/" + url + "/";
    }

    @Override
    public final String generateURL(final CategoryDataset dataset, final int row, final int column)
    {
        return getLabel(dataset, column).build.getNumber() + url;
    }

    /**
     * Gets the tool tip builder.
     *
     * @return the tool tip builder
     */
    public final ToolTipBuilder getToolTipBuilder()
    {
        return toolTipBuilder;
    }

    /**
     * Returns the Hudson build label at the specified column.
     *
     * @param dataset
     *            data set of values
     * @param column
     *            the column
     * @return the label of the column
     */
    private NumberOnlyBuildLabel getLabel(final CategoryDataset dataset, final int column)
    {
        return ( NumberOnlyBuildLabel ) dataset.getColumnKey(column);
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column)
    {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(getToolTipBuilder().getTooltip(dataset.getValue(row, column).intValue()));
        tooltip.append(" ");
        
        switch(row)
        {
            case 1:
                tooltip.append("Formating");
            break;
            case 2:
                tooltip.append("Performance");
            break;
            case 3:
                tooltip.append("Design");
            break;
            case 4:
                tooltip.append("Low security");
            break;
            default:
                tooltip.append("High Security");
            break;
        }
        return tooltip.toString();
    }
}