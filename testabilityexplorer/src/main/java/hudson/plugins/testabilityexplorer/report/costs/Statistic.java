package hudson.plugins.testabilityexplorer.report.costs;

import hudson.model.AbstractBuild;

import java.io.Serializable;

/**
 * Encapsulates a full Testability Explorer report.
 *
 * @author reik.schatz
 */
public class Statistic implements Serializable
{
    private AbstractBuild<?, ?> m_owner;
    /*
     * Apparently the mavenBuild is not correctly persisting the state of the owner object?
     * We always get an empty project reference. So hereby we explicitly save the project attribute
     * we need.
     */
    private String m_displayName;
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
        if(null!=owner.getProject()){
            m_displayName = owner.getProject().getDisplayName();
        }
    }

    public void sort()
    {
        CostSummary summary = getCostSummary();
        summary.sort();
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


    /**
     * Getter for m_displayName.
     * @return the m_displayName
     */
    public String getDisplayName() {
        return m_displayName;
    }
}
