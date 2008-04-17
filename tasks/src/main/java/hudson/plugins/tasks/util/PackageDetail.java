package hudson.plugins.tasks.util;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.model.JavaPackage;
import hudson.plugins.tasks.util.model.WorkspaceFile;

import java.util.Collection;

/**
 * Result object to visualize the package statistics of a module.
 */
public class PackageDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5315146140343619856L;
    /** The package to show the details for. */
    private final JavaPackage javaPackage;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param javaPackage
     *            the package to show the details for
     * @param header
     *            header to be shown on detail page
     */
    public PackageDetail(final AbstractBuild<?, ?> owner, final JavaPackage javaPackage, final String header) {
        super(owner, javaPackage.getAnnotations(), header);
        this.javaPackage = javaPackage;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getTitle() + " - " + javaPackage.getPackageCategoryName() + " " + javaPackage.getName();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return javaPackage.getName();
    }

    /**
     * Returns the maven module.
     *
     * @return the maven module
     */
    public JavaPackage getPackage() {
        return javaPackage;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<WorkspaceFile> getFiles() {
        return javaPackage.getFiles();
    }

    /** {@inheritDoc} */
    @Override
    public WorkspaceFile getFile(final String fileName) {
        return javaPackage.getFile(fileName);
    }
}

