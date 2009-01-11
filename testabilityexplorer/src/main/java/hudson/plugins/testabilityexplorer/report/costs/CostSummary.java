package hudson.plugins.testabilityexplorer.report.costs;

import hudson.plugins.testabilityexplorer.report.costs.ClassCost;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Encapsulates the overall cost for a single testability run.
 *
 * @author reik.schatz
 */
public class CostSummary implements Serializable, TestabilityCost
{
    private int m_excellent;
    private int m_good;
    private int m_needsWork;
    private int m_total;
    private Collection<ClassCost> m_costStack;

    public CostSummary(int excellent, int good, int needsWork, int total)
    {
        m_excellent = excellent;
        m_good = good;
        m_needsWork = needsWork;
        m_total = total;
    }

    public int getExcellent()
    {
        return m_excellent;
    }

    public int getGood()
    {
        return m_good;
    }

    public int getNeedsWork()
    {
        return m_needsWork;
    }

    public int getTotal()
    {
        return m_total;
    }

    public void addToCostStack(ClassCost classTestability)
    {
        if (m_costStack == null)
        {
            m_costStack = new ArrayList<ClassCost>();
        }
        m_costStack.add(classTestability);
    }

    public Collection<ClassCost> getCostStack()
    {
        return m_costStack == null ? new ArrayList<ClassCost>() : Collections.unmodifiableCollection(m_costStack);
    }

    public int getNumberOfClasses()
    {
        return getExcellent() + getGood() + getNeedsWork();
    }

    /**
     * Sorts the all costs contained in this summary highest first.
     */
    public void sort()
    {
        if (m_costStack != null)
        {
            for (ClassCost classCost : m_costStack)
            {
                classCost.sort();
            }
        }
    }
}
