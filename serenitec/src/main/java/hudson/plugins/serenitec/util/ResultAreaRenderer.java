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
 * <li>The tooltip is computed per column (i.e., per build) and shows the total
 * number of annotations for this build.</li>
 * <li>The link is also computed per column and links to the results for this
 * build.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
public class ResultAreaRenderer extends StackedAreaRenderer2
{

    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4683951507836348304L;
    /** Base URL of the graph links. */
    private final String url;
    /** Tooltip provider for the clickable map. */
    private final ToolTipBuilder toolTipBuilder;

    /**
     * Creates a new instance of <code>ResultAreaRenderer</code>.
     *
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     */
    public ResultAreaRenderer(final String url, final ToolTipProvider toolTipProvider)
    {
        toolTipBuilder = new ToolTipBuilder(toolTipProvider);
        this.url = "/" + url + "/";
    }

    @Override
    public final String generateURL(final CategoryDataset dataset, final int row, final int column)
    {
        return getLabel(dataset, column).build.getNumber() + url;
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column)
    {
        int number = 0;
        for (int index = 0; index < dataset.getRowCount(); index++)
        {
            final Number value = dataset.getValue(index, column);
            if (value != null)
            {
                number += value.intValue();
            }
        }
        return getToolTipBuilder().getTooltip(number);
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

    /**
     * Gets the tool tip builder.
     *
     * @return the tool tip builder
     */
    public final ToolTipBuilder getToolTipBuilder()
    {
        return toolTipBuilder;
    }
}