package hudson.plugins.testabilityexplorer.parser.converters;

import org.xmlpull.v1.XmlPullParser;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.TestabilityCost;

/**
 * Converts a &lt;class&gt; XML element into a {@link ClassCost}.
 *
 * @author reik.schatz
 */
public class ClassElementConverter extends ElementConverter
{
    /**
     * Constructs a new {@link ClassCost}.
     *
     * @param xpp XmlPullParser
     * @param root the {@link CostSummary} root
     * @return ClassCost
     */
    public TestabilityCost construct(XmlPullParser xpp, CostSummary root)
    {
        String namespace = getNamespace();
        String className = xpp.getAttributeValue(namespace, "class");
        int cost = toInt(xpp.getAttributeValue(namespace, "cost"), -1);

        ClassCost classTestability = new ClassCost(className, cost);
        root.addToCostStack(classTestability);
        return classTestability;
    }
}
