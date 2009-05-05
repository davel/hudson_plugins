package hudson.plugins.tasks.util;

import hudson.plugins.tasks.util.model.Priority;
import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Builds a graph showing all warnings by priority.
 *
 * @author Ulli Hafner
 */
public class PriorityGraph extends CategoryBuildResultGraph {
    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfAnnotations(Priority.LOW));
        series.add(current.getNumberOfAnnotations(Priority.NORMAL));
        series.add(current.getNumberOfAnnotations(Priority.HIGH));
        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createAreaChart(dataSet);
    }

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED};
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @java.lang.SuppressWarnings("serial")
    @SuppressWarnings("SIC")
    @Override
    protected CategoryItemRenderer createRenderer(final String pluginName, final ToolTipProvider toolTipProvider) {
        CategoryUrlBuilder url = new CategoryUrlBuilder(getRootUrl(), pluginName) {
            /** {@inheritDoc} */
            @Override
            protected String getDetailUrl(final int row) {
                if (row == 0) {
                    return Priority.LOW.name();
                }
                else if (row == 1) {
                    return Priority.NORMAL.name();
                }
                else {
                    return Priority.HIGH.name();
                }
            }
        };
        ToolTipBuilder toolTip = new ToolTipBuilder(toolTipProvider) {
            /** {@inheritDoc} */
            @Override
            protected String getShortDescription(final int row) {
                if (row == 0) {
                    return Messages.Trend_PriorityLow();
                }
                else if (row == 1) {
                    return Messages.Trend_PriorityNormal();
                }
                else {
                    return Messages.Trend_PriorityHigh();
                }
            }
        };
        return new AreaRenderer(url, toolTip);
    }
    // CHECKSTYLE:ON
}

