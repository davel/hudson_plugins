package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.detail.ClassCostDetail;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import hudson.model.AbstractBuild;

/**
 * Helper class to return detail object for details in the report.
 *
 * @author reik.schatz
 */
public class CostDetailBuilder implements DetailBuilder<Statistic>
{
    /**
     * Returns a detail from the specified <code>statistics</code>. The detail will be looked up
     * using the given <code>link</code>. The following details are supported right now:
     * <p>
     *  <code>class.</code> returns the first {@link ClassCostDetail} matching what is behind .class
     * </p>
     * @param link the dynamic link part as sent in by Stapler
     * @param statistics Collection
     * @return Object or <code>null</code>
     */
    public Object buildDetail(final String link, final AbstractBuild<?, ?> build, final Collection<Statistic> statistics)
    {
        Object dynamic = null;
        if (!StringUtils.isBlank(link) && link.startsWith("class."))
        {
            String className = StringUtils.substringAfter(link, "class.");
            if (!StringUtils.isBlank(className))
            {
                for (Statistic statistic : statistics)
                {
                    CostSummary summary = statistic.getCostSummary();
                    for (ClassCost classCost : summary.getCostStack())
                    {
                        if (className.equals(classCost.getName()))
                        {
                            ClassCostDetail classCostDetail = new ClassCostDetail();
                            classCostDetail.setOwner(build);
                            classCostDetail.setClassCost(classCost);
                            dynamic = classCostDetail;
                            break;
                        }
                    }
                }
            }
        }
        return dynamic;
    }
}
