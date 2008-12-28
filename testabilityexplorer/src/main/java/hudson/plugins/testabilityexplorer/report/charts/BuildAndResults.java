package hudson.plugins.testabilityexplorer.report.charts;

import hudson.model.AbstractBuild;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Helper class that wraps an AbstractBuild and a collection of Statistic.
 *
 * @author reik.schatz
 */
public class BuildAndResults
{
    private final AbstractBuild<?, ?> m_build;
    private final Collection<Statistic> m_statistics;

    public BuildAndResults(AbstractBuild<?, ?> build, Collection<Statistic> statistics)
    {
        m_build = build;
        m_statistics = new ArrayList<Statistic>();

        if (statistics != null)
        {
            for (Statistic statistic : statistics)
            {
                m_statistics.add(statistic);
            }
        }
    }

    public AbstractBuild<?, ?> getBuild()
    {
        return m_build;
    }

    public Collection<Statistic> getStatistics()
    {
        return new ArrayList<Statistic>(m_statistics);
    }
}
