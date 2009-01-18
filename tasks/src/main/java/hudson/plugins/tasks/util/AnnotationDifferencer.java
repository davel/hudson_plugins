package hudson.plugins.tasks.util;

import hudson.plugins.tasks.util.model.FileAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides several utility methods based on sets of annotations.
 *
 * @author Ulli Hafner
 */
public final class AnnotationDifferencer {
    /**
     * Returns the new annotations, i.e., the annotations that are in the actual build
     * but not in the previous.
     *
     * @param actual
     *            annotations in actual build
     * @param previous
     *            annotations in previous build
     * @return the new annotations
     */
    public static Set<FileAnnotation> getNewAnnotations(final Collection<FileAnnotation> actual, final Collection<FileAnnotation> previous) {
        return removeDuplicates(difference(actual, previous), difference(previous, actual));
    }


    /**
     * Computes the elements of the first set without the elements in the second
     * set.
     *
     * @param target
     *            the first set
     * @param other
     *            the second set
     * @return the difference of the sets
     */
    private static Set<FileAnnotation> difference(
            final Collection<FileAnnotation> target, final Collection<FileAnnotation> other) {
        Set<FileAnnotation> difference = new HashSet<FileAnnotation>(target);
        difference.removeAll(other);
        return difference;
    }

    /**
     * Removes the annotations from the {@code targetSet} that have the same
     * hash code as one annotation in the {@code otherSet}.
     *
     * @param targetSet
     *            the target set
     * @param otherSet
     *            the other set that is checked for duplicates
     * @return the unique annotations
     */
    private static Set<FileAnnotation> removeDuplicates(final Set<FileAnnotation> targetSet, final Set<FileAnnotation> otherSet) {
        Set<Long> otherHashCodes = extractHashCodes(otherSet);
        ArrayList<FileAnnotation> duplicates = new ArrayList<FileAnnotation>();
        for (FileAnnotation annotation : targetSet) {
            if (otherHashCodes.contains(annotation.getContextHashCode())) {
                duplicates.add(annotation);
            }
        }

        targetSet.removeAll(duplicates);
        return targetSet;
    }

    /**
     * Extracts the hash codes of the specified collection of annotations.
     *
     * @param annotations
     *            the annotations to get the hash codes from
     * @return the hash codes of the specified collection of annotations
     */
    private static HashSet<Long> extractHashCodes(final Set<FileAnnotation> annotations) {
        HashSet<Long> hashCodes = new HashSet<Long>();
        for (FileAnnotation annotation : annotations) {
            hashCodes.add(annotation.getContextHashCode());
        }
        return hashCodes;
    }

    /**
     * Returns the fixed annotations, i.e., the annotations that are in the previous build
     * but not in the actual.
     *
     * @param actual
     *            annotations in actual build
     * @param previous
     *            annotations in previous build
     * @return the fixed annotations
     */
    public static Set<FileAnnotation> getFixedAnnotations(final Collection<FileAnnotation> actual, final Collection<FileAnnotation> previous) {
        return removeDuplicates(difference(previous, actual), difference(actual, previous));
    }

    /**
     * Creates a new instance of <code>AnnotationDifferencer</code>.
     */
    private AnnotationDifferencer() {
        // prevents instantiation
    }
}

