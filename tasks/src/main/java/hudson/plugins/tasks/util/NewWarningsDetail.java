package hudson.plugins.tasks.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.FileAnnotation;

import java.util.Collection;

/**
 * Result object to visualize the new warnings in a build.
 *
 * @author Ulli Hafner
 */
public class NewWarningsDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5093487322493056475L;

    /**
     * Creates a new instance of <code>NewWarningsDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param newWarnings
     *            all new warnings in this build
     * @param header
     *            header to be shown on detail page
     */
    public NewWarningsDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> newWarnings, final String header) {
        super(owner, newWarnings, header, Hierarchy.PROJECT);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.NewWarningsDetail_Name();
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getName();
    }

    /**
     * Returns the dynamic result of this object (detail page for a source
     * file).
     *
     * @param link
     *            the source file to get the result for
     * @return the dynamic result of this object (detail page for a source
     *         file).
     */
    @Override
    public ModelObject getDynamic(final String link) {
        return new SourceDetail(getOwner(), getAnnotation(link));
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getModules();
    }
}

