/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:23 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;

/**
 * Provides tooltips for single or multiple items.
 *
 * @author Ulli Hafner
 */
public interface ToolTipProvider
{

    /**
     * Returns the tooltip for several items.
     *
     * @param numberOfItems
     *            the number of items to display the tooltip for
     * @return the tooltip for several items
     */
    String getMultipleItemsTooltip(int numberOfItems);

    /**
     * Returns the tooltip for exactly one item.
     *
     * @return the tooltip for exactly one item
     */
    String getSingleItemTooltip();
}
