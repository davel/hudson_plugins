/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:44 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util.model;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * Defines the priority of an annotation.
 *
 * @author Ulli Hafner
 */
public enum Priority
{

    /** High priority. */
    HIGH,
    /** Normal priority. */
    NORMAL,
    /** Low priority. */
    LOW;

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority
     *            priority as a String
     * @return enumeration value.
     */
    public static Priority fromString(final String priority)
    {
        return Priority.valueOf(StringUtils.upperCase(priority));
    }

    /**
     * Converts priorities for {@link XStream} deserialization.
     */
    public static final class PriorityConverter extends AbstractSingleValueConverter
    {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public boolean canConvert(final Class type)
        {
            return type.equals(Priority.class);
        }

        /** {@inheritDoc} */
        @Override
        public Object fromString(final String str)
        {
            return Priority.valueOf(str);
        }
    }

    /**
     * Returns a localized description of this priority.
     *
     * @return localized description of this priority
     */
    public String getLocalizedString()
    {
        if (this == HIGH)
        {
            return "oprioritye haute";
        }
        if (this == LOW)
        {
            return "Messages.Priority_Low()";
        }
        return "Messages.Priority_Normal()";
    }

    /**
     * Returns a long localized description of this priority.
     *
     * @return long localized description of this priority
     */
    public String getLongLocalizedString()
    {
        if (this == Priority.HIGH)
        {
            return "Messages.HighPriority()";
        }
        if (this == Priority.LOW)
        {
            return "Messages.LowPriority()";
        }
        return "Messages.NormalPriority()";
    }
}