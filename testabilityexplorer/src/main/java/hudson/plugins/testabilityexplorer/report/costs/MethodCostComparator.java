package hudson.plugins.testabilityexplorer.report.costs;

import java.util.Comparator;

/**
 * A {@link Comparator} to compare two {@link MethodCost} objects by their
 * overall testability.
 *
 * @author reik.schatz
 */
public final class MethodCostComparator implements Comparator<MethodCost>
{
    private static final MethodCostComparator COMPARATOR = new MethodCostComparator();

    private MethodCostComparator() { }

    public static MethodCostComparator getInstance()
    {
        return COMPARATOR;
    }

    /**
     * Compares the given two {@link MethodCost}'s by their overall.
     * @param methodCost first MethodCost
     * @param methodCost1 second MethodCost
     * @return int
     */
    public int compare(MethodCost methodCost, MethodCost methodCost1)
    {
        return ((Integer) methodCost1.getOverall()).compareTo(methodCost.getOverall());
    }
}
