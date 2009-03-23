package hudson.plugins.testabilityexplorer.report.costs;

import java.util.Comparator;

/**
 * A {@link Comparator} to compare two {@link ClassCost} objects by their
 * overall testability.
 *
 * @author reik.schatz
 */
public final class ClassCostComparator implements Comparator<ClassCost>
{
    private static final ClassCostComparator COMPARATOR = new ClassCostComparator();

    private ClassCostComparator() { }

    public static ClassCostComparator getInstance()
    {
        return COMPARATOR;
    }

    /**
     * Compares the given two {@link MethodCost}'s by their overall.
     * @param classCost first MethodCost
     * @param classCost1 second MethodCost
     * @return int
     */
    public int compare(ClassCost classCost, ClassCost classCost1)
    {
        return ((Integer) classCost1.getCost()).compareTo(classCost.getCost());
    }
}
