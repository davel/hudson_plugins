package hudson.plugins.testabilityexplorer.report.detail;

import hudson.model.ModelObject;
import hudson.model.AbstractBuild;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;

/**
 * Detailed view of a {@link ClassCost} which needs to get
 * access to the {@link AbstractBuild} as well.
 *
 * @author reik.schatz
 */
public class ClassCostDetail implements ModelObject
{
    private ClassCost m_classCost;
    private AbstractBuild<?, ?> m_owner;

    public ClassCost getClassCost()
    {
        return m_classCost;
    }

    public void setClassCost(ClassCost classCost)
    {
        m_classCost = classCost;
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
        return m_classCost == null ? "ClassCost" : m_classCost.getName();
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        if (m_owner != null)
        {
            s.append("Build: " + m_owner.toString() + ", ");
        }
        if (m_classCost != null)
        {
            s.append("ClassCost: " + m_classCost.toString());
        }
        return s.toString();
    }
}
