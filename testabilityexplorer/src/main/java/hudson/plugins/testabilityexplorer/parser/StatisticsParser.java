package hudson.plugins.testabilityexplorer.parser;


import hudson.plugins.testabilityexplorer.report.costs.Statistic;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * Parses testability explorer reports. These reports can be given as {@link File}'s or
 * {@link InputStream}'s. The {@link StatisticsParser} is also capable of merging multiple
 * reports.
 *
 * @author reik.schatz
 */
public abstract class StatisticsParser
{
    public abstract Collection<Statistic> parse(File inFile);
    public abstract Collection<Statistic> parse(InputStream inputStream);
}
