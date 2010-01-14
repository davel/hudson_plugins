package hudson.plugins.analysis.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.BoxRenderer;
import hudson.plugins.analysis.util.CategoryUrlBuilder;
import hudson.plugins.analysis.util.ToolTipBuilder;
import hudson.plugins.analysis.util.ToolTipProvider;

import hudson.util.ColorPalette;

/**
 * Builds a new versus fixed warnings graph for a specified result action.
 *
 * @author Ulli Hafner
 */
public class NewVersusFixedGraph extends CategoryBuildResultGraph {
    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "FIXED";
    }

    /** {@inheritDoc} */
    @Override
    public String getLabel() {
        return Messages.Trend_type_fixed();
    }

    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfNewWarnings());
        series.add(current.getNumberOfFixedWarnings());
        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createBlockChart(dataSet);
    }

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.RED, ColorPalette.BLUE};
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
                if (row == 1) {
                    return "fixed";
                }
                else {
                    return "new";
                }
            }
        };
        ToolTipBuilder toolTip = new ToolTipBuilder(toolTipProvider) {
            /** {@inheritDoc} */
            @Override
            protected String getShortDescription(final int row) {
                if (row == 1) {
                    return Messages.Trend_Fixed();
                }
                else {
                    return Messages.Trend_New();
                }
            }
        };
        return new BoxRenderer(url, toolTip);
    }
    // CHECKSTYLE:ON
}

