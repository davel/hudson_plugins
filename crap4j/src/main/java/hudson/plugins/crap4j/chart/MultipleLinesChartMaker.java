package hudson.plugins.crap4j.chart;

import hudson.util.ColorPalette;

import java.awt.BasicStroke;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;

public class MultipleLinesChartMaker extends AbstractChartMaker {
	
	public MultipleLinesChartMaker() {
		super();
	}
	
	@Override
	protected JFreeChart createRawChart(CategoryDataset dataset,
			String rangeAxisTitle) {
        JFreeChart result = ChartFactory.createLineChart(
                null,                   // chart title
                null,                   // unused
                rangeAxisTitle,          // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                false,                     // include legend
                true,                     // tooltips
                false                     // urls
        );
        NumberAxis secondAxis = new NumberAxis("second axis");
        result.getCategoryPlot().setRangeAxis(1, secondAxis);
        result.getCategoryPlot().setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        return result;
	}
	
	@Override
	protected void setupPlot(CategoryPlot plot) {
		super.setupPlot(plot);
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(2.0f));
        ColorPalette.apply(renderer);
	}
	
	@Override
	protected void setupRangeAxis(int axisID, NumberAxis rangeAxis) {
		super.setupRangeAxis(axisID, rangeAxis);
		rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
	}
}
