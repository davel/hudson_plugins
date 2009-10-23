package hudson.plugins.testabilityexplorer.report.health;

import hudson.model.HealthReport;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;

import java.util.Collection;
import org.jvnet.localizer.Localizable;

/**
 * A {@link ReportBuilder} that will compute health based on the specified
 * collection of statistics.
 *
 * @author reik.schatz
 */
public class TestabilityReportBuilder extends DrawingReportBuilder
{
    private final HealthCalculator m_healthCalculator;

    public TestabilityReportBuilder(ChartBuilder chartBuilder, HealthCalculator healthCalculator)
    {
        super(chartBuilder);
        m_healthCalculator = healthCalculator;
    }

    public HealthReport computeHealth(Collection<Statistic> results)
    {
        int score;
        Localizable description;

        int allClasses = 0;
        int excellentClasses = 0;
        for (Statistic result : results)
        {
            allClasses += getNumberOfClasses(result.getCostSummary());
            excellentClasses += getNumberOfExcellentClasses(result.getCostSummary());
        }

        HealthCalculator healthCalculator = m_healthCalculator;
        if (m_healthCalculator == null)
        {
            healthCalculator = new TemporaryHealthCalculator();
        }

        score = healthCalculator.calculate(allClasses, excellentClasses, 0, 0);
        if (score == 100)
        {
            description = Messages._TestabilityReportBuilder_GreatHealth();
        }
        else if (score > 80)
        {
            description = Messages._TestabilityReportBuilder_GoodHealth();
        }
        else if (score > 0)
        {
            description = Messages._TestabilityReportBuilder_BadHealth();
        }
        else
        {
            description = Messages._TestabilityReportBuilder_NoHealth();
        }

        return new HealthReport(score, description);
    }

    private int getNumberOfClasses(CostSummary costSummary)
    {
        return costSummary.getNumberOfClasses();
    }

    private int getNumberOfExcellentClasses(CostSummary costSummary)
    {
        return costSummary.getExcellent();
    }
}
