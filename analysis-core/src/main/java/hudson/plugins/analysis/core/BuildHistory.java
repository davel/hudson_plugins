package hudson.plugins.analysis.core;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.CheckForNull;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * History of build results of a specific plug-in. The plug-in is identified by
 * the corresponding {@link ResultAction} type.
 *
 * @author Ulli Hafner
 */
public class BuildHistory {
    /** The build to start the history from. */
    private final AbstractBuild<?, ?> baseline;
    /** Type of the action that contains the build results. */
    private final Class<? extends ResultAction<? extends BuildResult>> type;

    /**
     * Creates a new instance of {@link BuildHistory}.
     *
     * @param baseline
     *            the build to start the history from
     * @param type
     *            type of the action that contains the build results
     */
    public BuildHistory(final AbstractBuild<?, ?> baseline, final Class<? extends ResultAction<? extends BuildResult>> type) {
        this.baseline = baseline;
        this.type = type;
    }

    /**
     * Returns whether a reference build result exists.
     *
     * @return <code>true</code> if a reference build result exists.
     */
    private boolean hasReferenceResult() {
        return getReferenceAction() != null;
    }

    /**
     * Returns the annotations of the reference build.
     *
     * @return the annotations of the reference build
     */
    public AnnotationContainer getReferenceAnnotations() {
        ResultAction<? extends BuildResult> action = getReferenceAction();
        if (action != null) {
            return action.getResult().getContainer();
        }
        return new DefaultAnnotationContainer();
    }

    /**
     * Returns the action of the reference build.
     *
     * @return the action of the reference build, or <code>null</code> if no
     *         such build exists
     */
    private ResultAction<? extends BuildResult> getReferenceAction() {
        for (AbstractBuild<?, ?> build = baseline.getPreviousBuild(); build != null; build = build.getPreviousBuild()) {
            ResultAction<? extends BuildResult> action = build.getAction(type);
            if (action != null && action.isSuccessful()) {
                return action;
            }
        }
        return getPreviousAction(); // fallback, use previous build
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    public boolean hasPreviousResult() {
        return getPreviousAction() != null;
    }

    /**
     * Returns the action of the previous build.
     *
     * @return the action of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @CheckForNull
    private ResultAction<? extends BuildResult> getPreviousAction() {
        for (AbstractBuild<?, ?> build = baseline.getPreviousBuild(); build != null; build = build.getPreviousBuild()) {
            ResultAction<? extends BuildResult> action = build.getAction(type);
            if (action != null) {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns the previous build result.
     *
     * @return the previous build result
     * @see #hasPreviousResult()
     * @throws NoSuchElementException
     *             if there is no previous result
     */
    public BuildResult getPreviousResult() {
        ResultAction<? extends BuildResult> action = getPreviousAction();
        if (action != null) {
            return action.getResult();
        }
        throw new NoSuchElementException("No previous result available");
    }

    /**
     * Returns the new warnings as a difference between the specified collection
     * of warnings and the warnings of the reference build.
     *
     * @param annotations
     *            the warnings in the current build
     * @return the difference "current build" - "reference build"
     */
    public Collection<FileAnnotation> getNewWarnings(final Set<FileAnnotation> annotations) {
        if (hasReferenceResult()) {
            return AnnotationDifferencer.getNewAnnotations(annotations, getReferenceAnnotations().getAnnotations());
        }
        else {
            return annotations;
        }
    }

    /**
     * Returns the fixed warnings as a difference between the warnings of the
     * reference build and the specified collection of warnings.
     *
     * @param annotations
     *            the warnings in the current build
     * @return the difference "reference build" - "current build"
     */
    public Collection<FileAnnotation> getFixedWarnings(final Set<FileAnnotation> annotations) {
        if (hasReferenceResult()) {
            return AnnotationDifferencer.getFixedAnnotations(annotations, getReferenceAnnotations().getAnnotations());
        }
        else {
            return Collections.emptyList();
        }
    }
}

