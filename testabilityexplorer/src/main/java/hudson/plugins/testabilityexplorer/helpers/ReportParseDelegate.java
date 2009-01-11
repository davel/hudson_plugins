package hudson.plugins.testabilityexplorer.helpers;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.BuildIndividualReport;
import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.parser.XmlStatisticsParser;

/**
 * Scans for report files using the specified <code>reportFilenamePattern</code>. Any report files
 * found that way, will be parsed and turned into {@link Statistic}'s.
 *
 * @author reik.schatz
 */
public class ReportParseDelegate implements ParseDelegate
{
    private final String m_reportFilenamePattern;
    private final int m_threshold;
    private final int m_perClassThreshold;

    public ReportParseDelegate(String reportFilenamePattern, int threshold, int perClassThreshold)
    {
        m_reportFilenamePattern = reportFilenamePattern;
        m_threshold = threshold;
        m_perClassThreshold = perClassThreshold;
    }

    /**
     * Uses the <code>reportFilenamePattern</code> in this {@link ReportParseDelegate} to look
     * for report files which will then be delegated for parsing.
     */
    public boolean perform(BuildProxy build, BuildListener listener)
    {
        List<FilePath> filesToParse = getFilesToParse(build);
        Collection<Statistic> results = null;

        for (FilePath filePath : filesToParse)
        {
            final String pathStr = filePath.getRemote();
            final StatisticsParser parser = build.getStatisticsParser();
            results = parser.parse(new File(pathStr));
        }

        boolean successful = isSuccessful(results);
        flagBuild(successful, build);

        build.addAction(new BuildIndividualReport(results, build.getReportBuilder(), build.getDetailBuilder()));

        return successful;
    }

    protected void flagBuild(boolean successful, BuildProxy build)
    {
        if (!successful)
        {
            build.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Returns a list of {@link FilePath}'s that are supposed to be parsed.
     *
     * @param build the BuildProxy
     * @return list of FilePath objects or empty list
     */
    protected List<FilePath> getFilesToParse(BuildProxy build)
    {
        FilePath[] paths;
        try
        {
            paths = build.getModuleRoot().list(m_reportFilenamePattern);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to get report files using the specified report file pattern (" + m_reportFilenamePattern + ").");
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Unable to get report files using the specified report file pattern (" + m_reportFilenamePattern + ").");
        }

        List<FilePath> filesToParse = new ArrayList<FilePath>();
        for (FilePath path : paths)
        {
            if (!filesToParse.contains(path))
            {
                filesToParse.add(path);
            }
        }
        return filesToParse;
    }

    /**
     * Returns <code>true</code> if:
     * <ul>
     *  <li>none of the {@link Statistic}'s in the given collection has a higher total testability cost than the threshold in this {@link ReportParseDelegate}
     *  <li>none of the ClassCost's in the given {@link Statistic}'s has a higher individual testability cost than the per-class threshold in this {@link ReportParseDelegate}
     * </ul>
     * @param results a collection of {@link Statistic}'s
     * @return boolean
     */
    boolean isSuccessful(Collection<Statistic> results)
    {
        boolean successful = true;
        if (results != null)
        {
            for (Statistic result : results)
            {
                CostSummary costSummary = result.getCostSummary();
                int total = costSummary.getTotal();
                successful = total <= m_threshold;

                if (!successful)
                {
                    break;
                }

                for (ClassCost classCost : costSummary.getCostStack())
                {
                    int perClassCost = classCost.getCost();
                    successful = perClassCost <= m_perClassThreshold;

                    if (!successful)
                    {
                        break;
                    }
                }
            }
        }
        return successful;
    }
}
