package hudson.plugins.testabilityexplorer.report.charts;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataItem;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RectangleEdge;

import java.awt.*;

import hudson.util.ShiftedCategoryAxis;
import hudson.util.ColorPalette;

/**
 * Wraps the creation of {@link JFreeChart}'s.
 *
 * @author reik.schatz
 */
public class TestabilityChartBuilder implements ChartBuilder
{
    /** {@inheritDoc} */
    public JFreeChart createChart(final RangedTrend rangedTrend)
    {
        CategoryDataset dataset = rangedTrend.getCategoryDataset();
        
        JFreeChart chart = ChartFactory.createLineChart(
            null,                       // chart title
            null,                       // unused
            "cost",                    // range axis label
            dataset,                    // data
            PlotOrientation.VERTICAL,   // orientation
            true,                       // include legend
            true,                       // tooltips
            false                       // urls
        );

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        plot.setDomainAxis(domainAxis);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(rangedTrend.getUpperBoundRangeAxis());
        rangeAxis.setLowerBound(0);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setStroke(new BasicStroke(4.0f));
        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0,0,0,5.0));

        return chart;
    }
}
