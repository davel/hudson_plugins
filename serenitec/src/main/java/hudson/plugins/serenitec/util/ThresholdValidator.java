/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:24 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;

import hudson.util.FormValidation;

import org.apache.commons.lang.StringUtils;

/**
 * Validates a threshold parameter. A threshold must be an integer value greater
 * or equal 0.
 *
 * @author Ulli Hafner
 */
public class ThresholdValidator
{

    /** Error message. */
    private static final String MESSAGE = "Threshold must be an integer value greater or equal 0.";

    public static FormValidation check(final String value)
    {
        if (!StringUtils.isEmpty(value))
        {
            try
            {
                int integer = Integer.valueOf(value);
                if (integer < 0)
                {
                    return FormValidation.error(MESSAGE);
                }
            }
            catch (NumberFormatException exception)
            {
                return FormValidation.error(MESSAGE);
            }
        }
        return FormValidation.ok();
    }
}
