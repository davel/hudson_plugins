package hudson.plugins.testabilityexplorer.parser.converters;

import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.TestabilityCost;
import org.xmlpull.v1.XmlPullParser;

/**
 * Converts a &lt;testability&gt; XML element into a {@link CostSummary}.
 *
 * @author reik.schatz
 */
public class TestabilityElementConverter extends ElementConverter
{
    /**
     * Constructs a new {@link CostSummary}.
     *
     * @param xpp XmlPullParser
     * @param root the OverallCost root (which should be <code>null</code>)
     * @return OverallCost
     */
    public TestabilityCost construct(XmlPullParser xpp, CostSummary root)
    {
        String namespace = getNamespace();
        int excellent = toInt(xpp.getAttributeValue(namespace, "excellent"), -1);
        int good = toInt(xpp.getAttributeValue(namespace, "good"), -1);
        int needsWork = toInt(xpp.getAttributeValue(namespace, "needsWork"), -1);
        int overall = toInt(xpp.getAttributeValue(namespace, "overall"), -1);
        return new CostSummary(excellent, good, needsWork, overall);
    }
}
