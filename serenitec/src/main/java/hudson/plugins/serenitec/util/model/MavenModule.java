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
import java.util.Map;

/**
 * A serializable Java Bean class representing a maven module.
 *
 * @author Ulli Hafner
 */
public class MavenModule extends EntriesContainer
{

    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5467122430572804130L;
    /** Name of this module. */
    private String name; // NOPMD: backward compatibility
    /** All Java packages in this maven module (mapped by their name). */
    @SuppressWarnings("unused")
    private Map<String, Package> packageMapping; // NOPMD: backward compatibility
    /** The error message that denotes that the creation of the module has been failed. */
    private String error;

    /**
     * Creates a new instance of <code>MavenModule</code>. File handling is
     * performed in this class since the files are already mapped in the modules
     * of this project.
     */
    public MavenModule()
    {
        super(Hierarchy.MODULE);
    }

    /**
     * Creates a new instance of <code>MavenModule</code>.
     *
     * @param moduleName
     *            name of the module
     */
    public MavenModule(final String moduleName)
    {
        super(moduleName, Hierarchy.MODULE);
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve()
    {
        setHierarchy(Hierarchy.MODULE);
        rebuildMappings();
        if (name != null)
        {
            setName(name);
        }
        return this;
    }

    /**
     * Sets an error message that denotes that the creation of the module has
     * been failed.
     *
     * @param error
     *            the error message
     */
    public void setError(final String error)
    {
        this.error = error;
    }

    /**
     * Return whether this module has an error message stored.
     *
     * @return <code>true</code> if this module has an error message stored.
     */
    public boolean hasError()
    {
        return error != null;
    }

    /**
     * Returns the error message for this module.
     *
     * @return the error message for this module
     */
    public String getError()
    {
        return error;
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends EntriesContainer> getChildren()
    {
        return getPackages();
    }
}

