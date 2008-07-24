/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:44 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util.model;

import java.util.Collection;

/**
 * A serializable Java Bean class representing a Java package.
 *
 * @author Ulli Hafner
 */
public class Package extends EntriesContainer
{

    /** Unique identifier of this class. */
    private static final long serialVersionUID = 4034932648975191723L;
    /** Name of this package. */
    private String name; // NOPMD: backward compatibility

    /**
     * Creates a new instance of <code>JavaPackage</code>.
     *
     * @param packageName
     *            the name of this package
     */
    public Package(final String packageName)
    {
        super(packageName, Hierarchy.PACKAGE);
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve()
    {
        setHierarchy(Hierarchy.PACKAGE);
        rebuildMappings();
        if (name != null)
        {
            setName(name);
        }
        return this;
    }

    @Override
    protected Collection<? extends EntriesContainer> getChildren()
    {
        // TODO Auto-generated method stub
        return null;
    }
}

