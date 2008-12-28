package hudson.plugins.testabilityexplorer.publisher;

import hudson.plugins.testabilityexplorer.helpers.ParseDelegate;
import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.report.CostDetailBuilder;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;

/**
 * Creates new instances of {@link ParseDelegate}, {@link StatisticsParser}, {@link CostDetailBuilder} and {@link ReportBuilder}
 * which will be required to fully process the testability XML reports and build up Hudson reports based on the results.
 */
public interface ExtractAndBuildDelegate
{
    abstract ParseDelegate newParseDelegate();
    abstract StatisticsParser newStatisticsParser();
    abstract CostDetailBuilder newDetailBuilder();
    abstract ReportBuilder newReportBuilder();
}
