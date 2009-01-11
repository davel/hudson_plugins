package hudson.plugins.testabilityexplorer.report.costs;

import hudson.model.AbstractBuild;

import java.io.*;

import hudson.plugins.testabilityexplorer.report.costs.CostSummary;

/**
 * Encapsulates a full Testability Explorer report.
 *
 * @author reik.schatz
 */
public class Statistic implements Serializable
{
    private AbstractBuild<?, ?> m_owner;
    private CostSummary m_costSummary;

    public Statistic(CostSummary costSummary)
    {
        m_costSummary = costSummary;
    }

    public CostSummary getCostSummary()
    {
        return m_costSummary;
    }

    public AbstractBuild<?, ?> getOwner()
    {
        return m_owner;
    }

    public void setOwner(AbstractBuild<?, ?> owner)
    {
        m_owner = owner;
    }

    public void sort()
    {
        CostSummary summary = getCostSummary();
        for (ClassCost classCost : summary.getCostStack())
        {
            classCost.sort();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Statistic statistic = (Statistic) o;

        if (m_owner != null)
        {
            if (statistic.m_owner == null)
            {
                return false;
            }
            else
            {
                return m_owner.toString().equals(statistic.m_owner.toString());
            }
        }
        else if (statistic.m_owner != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return m_owner != null ? m_owner.toString().hashCode() : 0;
    }
}
