package hudson.plugins.testabilityexplorer.report.detail;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.testabilityexplorer.report.costs.MethodCost;

/**
 * Detailed view of a {@link MethodCost} which needs to get
 * access to the {@link AbstractBuild} as well.
 *
 * @author reik.schatz
 */
public class MethodCostDetail implements ModelObject
{
    private MethodCost m_methodCost;
    private AbstractBuild<?, ?> m_owner;

    public MethodCost getMethodCost()
    {
        return m_methodCost;
    }

    public void setMethodCost(MethodCost methodCost)
    {
        m_methodCost = methodCost;
    }

    public AbstractBuild<?, ?> getOwner()
    {
        return m_owner;
    }

    public void setOwner(AbstractBuild<?, ?> owner)
    {
        m_owner = owner;
    }

    public String getDisplayName()
    {
        return m_methodCost == null ? "MethodCost" : m_methodCost.getName();
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        if (m_owner != null)
        {
            s.append("Build: " + m_owner.toString() + ", ");
        }
        if (m_methodCost != null)
        {
            s.append("MethodCost: " + m_methodCost.toString());
        }
        return s.toString();
    }
}
