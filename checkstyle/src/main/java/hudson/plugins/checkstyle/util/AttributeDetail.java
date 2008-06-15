package hudson.plugins.checkstyle.util;

import hudson.model.AbstractBuild;
import hudson.plugins.checkstyle.util.model.AnnotationContainer;
import hudson.plugins.checkstyle.util.model.FileAnnotation;

import java.util.Collection;

/**
 * Result object to visualize the statistics of a category.
 *
 * @author Ulli Hafner
 */
public class AttributeDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    private final String attributeName;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the module to show the details for
     * @param header
     *            header to be shown on detail page
     */
    public AttributeDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final String header, final String name) {
        super(owner, annotations, header, Hierarchy.PROJECT);
        attributeName = name;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return attributeName;
    }

    /**
     * Returns a tooltip showing the distribution of priorities for the selected
     * category.
     *
     * @param category
     *            the category to show the distribution for
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip(final String category) {
        return "TODO";
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getModules();
    }
}

