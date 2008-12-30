package hudson.plugins.testabilityexplorer.report.health;

import hudson.model.HealthReport;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.category.CategoryDataset;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.charts.RangedTrend;

import java.util.Collection;

/**
 * The ReportBuilder is responsible for different tasks that relate to
 * reporting activities such as creating {@link HealthReport}'s or initializing
 * charts.
 *
 * @author reik.schatz
 */
public interface ReportBuilder
{
    HealthReport computeHealth(Collection<Statistic> results);

    JFreeChart createGraph(final RangedTrend rangedTrend);
}
