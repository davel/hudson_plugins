package hudson.plugins.helpers;

import hudson.util.ChartUtil;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.IOException;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 09-Jan-2008 21:30:15
 */
public class GraphHelper
{
    /**
     * Do not instantiate GraphHelper.
     */
    private GraphHelper()
    {
    }

    /**
     * Getter for property 'graphUnsupported'.
     *
     * @return Value for property 'graphUnsupported'.
     */
    public static boolean isGraphUnsupported()
    {
        return ChartUtil.awtProblem;
    }

    public static void redirectWhenGraphUnsupported( StaplerResponse rsp, StaplerRequest req )
        throws IOException
    {
        // not available. send out error message
        rsp.sendRedirect2( req.getContextPath() + "/images/headless.png" );
    }

    public static JFreeChart buildChart( CategoryDataset dataset, String yAxisLabel )
    {

        final JFreeChart chart = ChartFactory.createLineChart( null,                   // chart title
                                                               null,                   // unused
                                                               yAxisLabel,                    // range axis label
                                                               dataset,                  // data
                                                               PlotOrientation.VERTICAL, // orientation
                                                               true,                     // include legend
                                                               true,                     // tooltips
                                                               false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        final LegendTitle legend = chart.getLegend();
        legend.setPosition( RectangleEdge.RIGHT );

        chart.setBackgroundPaint( Color.white );

        final CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint( null );
        plot.setRangeGridlinesVisible( true );
        plot.setRangeGridlinePaint( Color.black );

        CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
//        rangeAxis.setUpperBound(100);
//        rangeAxis.setLowerBound(0);

        plot.setRenderer( new StackedAreaRenderer2() );
////        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
////        renderer.setStroke(new BasicStroke(2.0f));
//        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

        return chart;
    }
}
