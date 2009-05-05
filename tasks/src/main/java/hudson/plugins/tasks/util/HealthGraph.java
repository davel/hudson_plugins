package hudson.plugins.tasks.util;

import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Builds a graph showing all warnings by health descriptor.
 *
 * @author Ulli Hafner
 */
public class HealthGraph extends CategoryBuildResultGraph {
    /** The health descriptor. */
    private final AbstractHealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthGraph}.
     *
     * @param healthDescriptor the builder to create the graph
     */
    public HealthGraph(final AbstractHealthDescriptor healthDescriptor) {
        this.healthDescriptor = healthDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>(3);
        int remainder = current.getNumberOfAnnotations();

        if (healthDescriptor.isHealthyReportEnabled()) {
            series.add(Math.min(remainder, healthDescriptor.getHealthyAnnotations()));

            int range = healthDescriptor.getUnHealthyAnnotations() - healthDescriptor.getHealthyAnnotations();
            remainder -= healthDescriptor.getHealthyAnnotations();
            if (remainder > 0) {
                series.add(Math.min(remainder, range));
            }
            else {
                series.add(0);
            }

            remainder -= range;
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }
        else if (healthDescriptor.isThresholdEnabled()) {
            series.add(Math.min(remainder, healthDescriptor.getMinimumAnnotations()));

            remainder -= healthDescriptor.getMinimumAnnotations();
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }

        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createAreaChart(dataSet);
    }

    /**
     * Returns whether to use three or two colors for the graph.
     *
     * @return <code>true</code> if the graph should use three colors,
     *         <code>false</code> if the graph should use two colors.
     */
    private boolean useThreeColors() {
        return healthDescriptor.isHealthyReportEnabled() || !healthDescriptor.isThresholdEnabled();
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @java.lang.SuppressWarnings("serial")
    @SuppressWarnings("SIC")
    @Override
    protected CategoryItemRenderer createRenderer(final String pluginName, final ToolTipProvider toolTipProvider) {
        SerializableUrlGenerator createUrlGenerator = new CategoryUrlBuilder(getRootUrl(), pluginName);
        SerializableToolTipGenerator createToolTipGenerator = new SerializableToolTipGenerator() {
            /** {@inheritDoc} */
            public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
                int number = 0;
                for (int index = 0; index < dataset.getRowCount(); index++) {
                    Number value = dataset.getValue(index, column);
                    if (value != null) {
                        number += value.intValue();
                    }
                }
                return toolTipProvider.getTooltip(number);
            }
        };
        return new AreaRenderer(createUrlGenerator, createToolTipGenerator);
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        if (useThreeColors()) {
            return new Color[] {ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED};
        }
        else {
            return new Color[] {ColorPalette.BLUE, ColorPalette.RED};
        }
    }
}

