package hudson.plugins.testabilityexplorer.report.health;

import org.jfree.chart.JFreeChart;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.RangedTrend;

/**
 * A {@link ReportBuilder} that can created Charts.
 *
 * @author reik.schatz
 */
public abstract class DrawingReportBuilder implements ReportBuilder
{
    private final ChartBuilder m_chartBuilder;

    protected DrawingReportBuilder(ChartBuilder chartBuilder)
    {
        m_chartBuilder = chartBuilder;
    }

    /**
     * Returns a {@link JFreeChart} based on the given {@link RangedTrend}. Will
     * never return {@code null}.
     *
     * @throws IllegalArgumentException if the given rangedTrend is {@code null}
     *
     * @param rangedTrend a RangedTrend to feed the JFreeChart with
     * @return JFreeChart
     */
    public JFreeChart createGraph(final RangedTrend rangedTrend)
    {
        if (rangedTrend == null)
        {
            throw new IllegalArgumentException("Parameter rangedTrend must not be null.");
        }

        ChartBuilder chartBuilder = m_chartBuilder;
        if (chartBuilder == null)
        {
            chartBuilder = new TestabilityChartBuilder();
        }
        return chartBuilder.createChart(rangedTrend);
    }
}
