package hudson.plugins.testabilityexplorer.report.costs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates the overall cost for a single testability run.
 *
 * @author reik.schatz
 */
public class CostSummary implements Serializable, TestabilityCost {

    public static final double WEIGHT_TO_EMPHASIZE_EXPENSIVE_METHODS = 1.5;

    private int m_excellent;

    private int m_good;

    private int m_needsWork;

    private int m_total;

    private List<ClassCost> m_costStack;

    public CostSummary(int excellent, int good, int needsWork, int total) {
        m_excellent = excellent;
        m_good = good;
        m_needsWork = needsWork;
        m_total = total;
    }

    public int getExcellent() {
        return m_excellent;
    }

    public int getGood() {
        return m_good;
    }

    public int getNeedsWork() {
        return m_needsWork;
    }

    public int getTotal() {
        return m_total;
    }

    public void addToCostStack(ClassCost classTestability) {
        if (m_costStack == null) {
            m_costStack = new ArrayList<ClassCost>();
        }
        m_costStack.add(classTestability);
    }

    public Collection<ClassCost> getCostStack() {
        return m_costStack == null ? new ArrayList<ClassCost>() : Collections
                .unmodifiableCollection(m_costStack);
    }

    public int getNumberOfClasses() {
        return getExcellent() + getGood() + getNeedsWork();
    }

    public CostSummary merge(CostSummary costs, double weight) {
        if (null != costs) {
            m_excellent += costs.getExcellent();
            m_good += costs.getGood();
            m_needsWork += costs.getNeedsWork();
            if (m_costStack == null) {
                m_costStack = new ArrayList<ClassCost>();
            }
            m_costStack.addAll(costs.getCostStack());
            calculateTotal(weight);
        }
        return this;
    }

    protected void calculateTotal(double weight) {
        WeightedAverage total = weight < 0 ? new WeightedAverage(
                WEIGHT_TO_EMPHASIZE_EXPENSIVE_METHODS) : new WeightedAverage(weight);
        for (Iterator<ClassCost> iterator = m_costStack.iterator(); iterator.hasNext();) {
            ClassCost cost = iterator.next();
            total.addValue(cost.getCost());
        }
        m_total = Double.valueOf(total.getAverage()).intValue();
    }

    /**
     * Sorts the all costs contained in this summary highest first.
     */
    public void sort() {
        if (m_costStack != null) {
            Collections.sort(m_costStack, ClassCostComparator.getInstance());
            for (ClassCost classCost : m_costStack) {
                classCost.sort();
            }
        }
    }

    private class WeightedAverage {

        public static final double WEIGHT = 0.3;

        private final double weight;

        private double overallSum = 0;

        private double overallSqr = 0;

        public WeightedAverage() {
            this(WEIGHT);
        }

        public WeightedAverage(double weight) {
            this.weight = weight;
        }

        public void addValue(long value) {
            overallSqr += Math.pow(value, weight + 1);
            overallSum += Math.pow(value, weight);
        }

        public double getAverage() {
            return overallSqr / overallSum;
        }

    }
}
