package hudson.plugins.testabilityexplorer.report.charts;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Creates Charts that may be used for reporting.
 *
 * @author reik.schatz
 */
public interface ChartBuilder
{
    /**
     * Creates a {@link JFreeChart} based on the given {@link RangedTrend}.
     */
    JFreeChart createChart(final RangedTrend rangedTrend);
}
