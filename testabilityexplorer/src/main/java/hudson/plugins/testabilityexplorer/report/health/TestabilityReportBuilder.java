package hudson.plugins.testabilityexplorer.report.health;

import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.model.HealthReport;

import java.util.Collection;

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
        String description = "";

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
            description = "Testability is excellent.";
        }
        else if (score > 80)
        {
            description = "Testability almost excellent.";
        }
        else if (score > 0)
        {
            description = "Testability need to be improved.";
        }
        else
        {
            description = "Testability is awful.";
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
