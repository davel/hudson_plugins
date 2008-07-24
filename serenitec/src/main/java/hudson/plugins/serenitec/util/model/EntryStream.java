/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:44 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util.model;

import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.util.StringConverter2;
import hudson.util.XStream2;

/**
 * An XStream for annotations.
 *
 * @author Ulli Hafner
 */
public class EntryStream extends XStream2
{

    /**
     * Creates a new instance of <code>AnnotationStream</code>.
     */
    public EntryStream()
    {
        super();

        alias("entry", ReportEntry.class);
        registerConverter(new StringConverter2(), 100);
        registerConverter(new Priority.PriorityConverter(), 100);
        addImmutableType(Priority.class);
    }
}

