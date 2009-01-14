package hudson.plugins.testabilityexplorer.report.costs;

import hudson.plugins.testabilityexplorer.utils.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates the testability of a single method.
 *
 * @author reik.schatz
 */
public class MethodCost implements Serializable, TestabilityCost
{
    private String m_name;
    private int m_cyclomatic;
    private int m_global;
    private int m_line;
    private int m_lod;
    private int m_overall;
    private String m_reason;
    private List<MethodCost> m_costStack;

    public MethodCost(String name, int cyclomatic, int global, int line, int lod, int overall, String reason)
    {
        m_name = name;
        m_cyclomatic = cyclomatic;
        m_global = global;
        m_line = line;
        m_lod = lod;
        m_overall = overall;
        m_reason = reason;
    }

    public void addToCostStack(MethodCost methodTestability)
    {
        if (m_costStack == null)
        {
            m_costStack = new ArrayList<MethodCost>();
        }
        m_costStack.add(methodTestability);
    }

    public int getSubCostSize()
    {
        return getCostStack().size();
    }

    public String getName()
    {
        return m_name == null ? "" : m_name;
    }

    /**
     * This will return a shorter version of {@link MethodCost#getName()}. The fully qualified class names of return
     * values and parameters will be shortened to be just the class name without package.
     * @return String
     */
    public String getDisplayName()
    {
        return StringUtils.abbreviate(getName(), 130);
    }

    public int getCyclomatic()
    {
        return m_cyclomatic;
    }

    public int getGlobal()
    {
        return m_global;
    }

    public int getLine()
    {
        return m_line;
    }

    public int getLod()
    {
        return m_lod;
    }

    public int getOverall()
    {
        return m_overall;
    }

    public String getReason()
    {
        return m_reason == null ? "" : m_reason;
    }

    public Collection<MethodCost> getCostStack()
    {
        return m_costStack == null ? new ArrayList<MethodCost>() : Collections.unmodifiableCollection(m_costStack);
    }

    /**
     * Sorts the all costs contained in this MethodCost highest first.
     */
    public void sort()
    {
        if (m_costStack != null)
        {
            Collections.sort(m_costStack, MethodCostComparator.getInstance());
            for (MethodCost methodCost : m_costStack)
            {
                methodCost.sort();
            }
        }
    }
}
