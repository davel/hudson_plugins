package hudson.plugins.testabilityexplorer.report.charts;

import hudson.model.AbstractBuild;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import org.jfree.data.category.CategoryDataset;

import java.util.Collection;
import java.util.List;

/**
 * Helper class that is able to construct a JFree {@link CategoryDataset} which may be used to draw a line chart for the overall testability.
 * A {@link RangedOverallTrend} can also calculate the corrent upper bound Integer value which may be used to limit the range
 * axis in size.
 *
 * @author reik.schatz
 */
public class RangedOverallTrend extends RangedTrend
{
    public static final CostTemplate TOTAL_COST_TEMPLATE   = new CostTemplate()
    {
        public int getCost(Statistic statistic)
        {
            return statistic.getCostSummary().getTotal();
        }
    };

    public RangedOverallTrend(List<BuildAndResults> items)
    {
        super(items);
    }

    /**
     * Returns an Integer which may be used in a JFree line chart to call {@link org.jfree.chart.axis.NumberAxis#setUpperBound}
     * with a correct value.
     *
     * @return int
     */
    public int getUpperBoundRangeAxis()
    {
        int maxCost = RangedTrend.DEFAULT_RANGE_AXIS;  // default range on the y-axis

        CostTemplate totalCostTemplate = RangedOverallTrend.TOTAL_COST_TEMPLATE;
        for (BuildAndResults buildAndResults : getItems())
        {
            Collection<Statistic> results = buildAndResults.getStatistics();
            int totalCost = summarizeCost(results, totalCostTemplate);
            maxCost = Math.max(maxCost, totalCost);
        }

        return maxCost > RangedTrend.DEFAULT_RANGE_AXIS ? maxCost + 10 : maxCost;    // add some space on top
    }

    /**
     * Builds and returns a {@link CategoryDataset} represents the overall testability trend. This
     * {@link CategoryDataset} can be used to draw a JFree line chart.
     *
     * @return CategoryDataset
     */
    public CategoryDataset getCategoryDataset()
    {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        CostTemplate totalCostTemplate = RangedOverallTrend.TOTAL_COST_TEMPLATE;

        Collection<Statistic> previousNonEmptyResults = null;   // this is needed so that the graphline will not be interrupted
        for (BuildAndResults buildAndResults : getItems())
        {
            AbstractBuild<?, ?> build = buildAndResults.getBuild();
            Collection<Statistic> results = buildAndResults.getStatistics();
            if (results.isEmpty())
            {
                results = previousNonEmptyResults != null ? previousNonEmptyResults : results;
            }
            else
            {
                previousNonEmptyResults = results;
            }

            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
            dsb.add(summarizeCost(results, totalCostTemplate), "overall", label);
        }

        return dsb.build();
    }
}
