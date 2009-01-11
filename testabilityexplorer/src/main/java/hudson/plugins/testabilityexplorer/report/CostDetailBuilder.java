package hudson.plugins.testabilityexplorer.report;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.report.detail.ClassCostDetail;
import hudson.plugins.testabilityexplorer.report.detail.MethodCostDetail;
import hudson.plugins.testabilityexplorer.utils.TypeConverterUtil;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;

import org.apache.commons.lang.StringUtils;
import hudson.model.AbstractBuild;

/**
 * Helper class to return detail object for details in the report.
 *
 * @author reik.schatz
 */
public class CostDetailBuilder implements DetailBuilder<Statistic>
{
    private static final Pattern LINE_PATTERN = Pattern.compile("\\:line\\.([\\d]*)[\\/]?\\Z", Pattern.DOTALL);

    /**
     * Returns a detail from the specified <code>statistics</code>. The detail will be looked up
     * using the given <code>link</code> and <code>originalRequestUri</code>. The following details are supported right now:
     * <p>
     *  <code>class.</code> returns the first {@link ClassCostDetail} matching what is behind .class
     * </p>
     * @param link the dynamic link part as sent in by Stapler
     * @param statistics Collection
     * @return Object or <code>null</code>
     */
    public Object buildDetail(final String link, final String originalRequestUri, final AbstractBuild<?, ?> build, final Collection<Statistic> statistics)
    {
        Object dynamic = null;
        if (!StringUtils.isBlank(link) && link.startsWith("class."))
        {
            String className = StringUtils.substringAfter(link, "class.");
            if (className.contains(":"))
            {
                className = StringUtils.substringBefore(className, ":");    
            }
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

                            MethodCost methodCost = lookupMethodCost(classCost, originalRequestUri);
                            if (methodCost != null)
                            {
                                MethodCostDetail methodCostDetail = new MethodCostDetail();
                                methodCostDetail.setMethodCost(methodCost);
                                methodCostDetail.setOwner(build);
                                dynamic = methodCostDetail;
                            }

                            break;
                        }
                    }
                }
            }
        }
        return dynamic;
    }

    /**
     * Expects a <code>originalRequestUri</code> that ends with <code>/line.xx</code>, where xx is
     * a line number. This method will return a MethodCost whose line number property matches the
     * line number in the request URI. Will return <code>null</code> if no such MethodCost exists
     * or the given <code>originalRequestUri</code> is invalid.
     * @param classCost a ClassCost to search
     * @param originalRequestUri request URI
     * @return MethodCost or <code>null</code>
     */
    MethodCost lookupMethodCost(final ClassCost classCost, final String originalRequestUri)
    {
        MethodCost cost = null;
        if (!StringUtils.isBlank(originalRequestUri))
        {
            String lineNumber = findMatch(originalRequestUri);
            if (lineNumber != null)
            {
                int line = TypeConverterUtil.toInt(lineNumber, -1);
                for (MethodCost methodCost : classCost.getCostStack())
                {
                    if (methodCost.getLine() == line)
                    {
                        cost = methodCost;
                        break;
                    }
                }
            }
        }
        return cost;
    }

    String findMatch(final String originalRequestUri)
    {
        String match = null;
        for (Matcher m = LINE_PATTERN.matcher(originalRequestUri); m.find(); )
        {
            MatchResult matchResult = m.toMatchResult();
            match = matchResult.group(1);
            break;
        }
        return match;
    }
}
