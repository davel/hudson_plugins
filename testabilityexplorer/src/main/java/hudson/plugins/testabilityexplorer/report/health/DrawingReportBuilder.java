package hudson.plugins.testabilityexplorer.report.health;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;

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
     * Returns a {@link JFreeChart} based on the given {@link CategoryDataset}. Will
     * never return {@code null}.
     *
     * @throws IllegalArgumentException if the given dataset is null
     *
     * @param dataset a CategoryDataset to feed the JFreeChart with
     * @return CategoryDataset
     */
    public JFreeChart createGraph(final CategoryDataset dataset)
    {
        if (dataset == null)
        {
            throw new IllegalArgumentException("Parameter dataset must not be null.");
        }

        ChartBuilder chartBuilder = m_chartBuilder;
        if (chartBuilder == null)
        {
            chartBuilder = new TestabilityChartBuilder();
        }
        return chartBuilder.createChart(dataset);
    }
}
