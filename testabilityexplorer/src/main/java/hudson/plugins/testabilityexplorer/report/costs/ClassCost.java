package hudson.plugins.testabilityexplorer.report.costs;

import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.utils.StringUtil;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates the testability of a single class.
 *
 * @author reik.schatz
 */
public class ClassCost implements Serializable, TestabilityCost
{
    private String m_name;
    private int m_cost;
    private Collection<MethodCost> m_costStack;

    public ClassCost(String name, int cost)
    {
        m_name = name;
        m_cost = cost;
    }

    public void addToCostStack(MethodCost methodTestability)
    {
        if (m_costStack == null)
        {
            m_costStack = new ArrayList<MethodCost>();
        }
        m_costStack.add(methodTestability);
    }

    public String getName()
    {
        return m_name;
    }

    /**
     * This will return a shorter version of {@link ClassCost#getName()}. The fully qualified class names of return
     * values and parameters will be shortened to be just the class name without package.
     * @return String
     */
    public String getDisplayName()
    {
        return StringUtil.stripPackages(getName());
    }

    public int getCost()
    {
        return m_cost;
    }

    public Collection<MethodCost> getCostStack()
    {
        return Collections.unmodifiableCollection(m_costStack);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (m_name != null)
        {
            sb.append("Name: " + m_name + ", ");
        }
        sb.append("Cost: " + m_cost);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassCost classCost = (ClassCost) o;

        if (m_name != null ? !m_name.equals(classCost.m_name) : classCost.m_name != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return m_name != null ? m_name.hashCode() : 0;
    }
}
