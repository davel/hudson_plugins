/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/23 12:05:04 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;


import hudson.plugins.serenitec.util.model.EntriesContainer;
import hudson.plugins.serenitec.util.model.MavenModule;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

public class Project extends EntriesContainer
{

    /**
     * SERIAL UID
     */
    private static final long serialVersionUID = 771741031245139227L;
    private String            workspacePath;
    /** Determines whether a module with an error is part of this project. */
    private boolean           hasModuleError;
    /** The error message that denotes that why project creation has been failed. */
    private String            error;

    /**
     * Creates a new instance of {@link JavaProject}.
     */
    public Project() {

        super(Hierarchy.PROJECT);
    }

    /**
     * Appends the error message to the project error messages.
     * 
     * @param additionalError
     *            the new error message to add
     */
    public void addError(final String additionalError) {

        if (StringUtils.isEmpty(error)) {
            error = additionalError;
        } else {
            error = error + "\n" + additionalError;
        }
    }

    /**
     * Adds the specified module with its annotations to this project.
     * 
     * @param module
     *            the module to add
     */
    public void addModule(final MavenModule module) {

        addEntries(module.getEntries());
        if (module.hasError()) {
            hasModuleError = true;
            addError(module.getError());
        }
    }

    /**
     * Adds the specified modules with their annotations to this project.
     * 
     * @param modules
     *            the modules to add
     */
    public void addModules(final Collection<MavenModule> modules) {

        for (final MavenModule mavenModule : modules) {
            addModule(mavenModule);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends EntriesContainer> getChildren() {

        return getModules();
    }

    /**
     * Returns the error message that denotes that why project creation has been failed.
     * 
     * @return the error message that denotes that why project creation has been failed.
     */
    public String getError() {

        return error;
    }

    /**
     * Returns the root path of the workspace files.
     * 
     * @return the workspace path
     */
    public String getWorkspacePath() {

        return workspacePath;
    }

    /**
     * Returns whether a module with an error is part of this project.
     * 
     * @return <code>true</code> if at least one module has an error.
     */
    public boolean hasError() {

        return hasModuleError || error != null;
    }

    /**
     * Rebuilds the priorities mapping.
     * 
     * @return the created object
     */
    private Object readResolve() {

        setHierarchy(Hierarchy.PROJECT);
        rebuildMappings();
        return this;
    }

    /**
     * Sets the error message that denotes that why project creation has been failed.
     * 
     * @param error
     *            the new error message
     */
    public void setError(final String error) {

        this.error = error;
    }

    /**
     * Sets the root path of the workspace files.
     * 
     * @param workspacePath
     *            path to workspace
     */
    public void setWorkspacePath(final String workspacePath) {

        this.workspacePath = workspacePath;
    }
    /**
     * 
     * 
     */

}
