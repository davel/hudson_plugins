package hudson.plugins.testabilityexplorer.report.charts;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;

/**
 * Selects and returns cost values.
 *
 * @author reik.schatz
 */
public abstract class CostTemplate
{
    public abstract int getCost(Statistic statistic);
}
