package hudson.plugins.crap4j.chart;

import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

public abstract class AbstractChartMaker {
	
    private static final double PADDING = 5.0;

	public AbstractChartMaker() {
		super();
	}
	
	public JFreeChart createChart(CategoryDataset dataset, String rangeAxisTitle) {
		JFreeChart result = createRawChart(dataset, rangeAxisTitle);
		result.setBackgroundPaint(Color.WHITE);
		setupPlot(result.getCategoryPlot());
		return result;
	}
	
	protected abstract JFreeChart createRawChart(CategoryDataset dataset, String rangeAxisTitle);
	
	protected void setupPlot(CategoryPlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        
        int axisCount = plot.getRangeAxisCount();
        for (int i = 0; i < axisCount; i++) {
            setupRangeAxis(i, (NumberAxis) plot.getRangeAxis(i));
		}

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(PADDING, 0, 0, PADDING));
	}
	
	protected void setupRangeAxis(int axisID, NumberAxis rangeAxis) {
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLowerBound(0);
	}
}
