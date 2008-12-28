package hudson.plugins.testabilityexplorer.parser.converters;

import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.TestabilityCost;
import org.xmlpull.v1.XmlPullParser;

import java.util.Collection;

/**
 * Converts a &lt;cost&gt; XML element into a {@link MethodCost}.
 *
 * @author reik.schatz
 */
public class CostElementConverter extends ElementConverter
{
    /**
     * Constructs a new {@link MethodCost}.
     *
     * @param xpp XmlPullParser
     * @param root the {@link CostSummary} root
     * @return MethodCost
     */
    public TestabilityCost construct(XmlPullParser xpp, CostSummary root)
    {
        String namespace = getNamespace();
        int cyclomatic = toInt(xpp.getAttributeValue(namespace, "cyclomatic"), -1);
        int global = toInt(xpp.getAttributeValue(namespace, "global"), -1);
        int line = toInt(xpp.getAttributeValue(namespace, "line"), -1);
        int lod = toInt(xpp.getAttributeValue(namespace, "lod"), -1);
        String method = xpp.getAttributeValue(namespace, "method");
        int overall = toInt(xpp.getAttributeValue(namespace, "overall"), -1);
        String reason = xpp.getAttributeValue(namespace, "reason");

        MethodCost methodTestability = new MethodCost(method, cyclomatic, global, line, lod, overall, reason);

        Collection<ClassCost> classTestabilities = root.getCostStack();
        int lastItemIndex = classTestabilities.size() -1;
        ClassCost classTestability = (ClassCost) classTestabilities.toArray() [lastItemIndex];

        Collection<MethodCost> methodTestabilities = classTestability.getCostStack();
        lastItemIndex = methodTestabilities.size() -1;
        MethodCost lastMethodTestability = (MethodCost) methodTestabilities.toArray() [lastItemIndex];
        lastMethodTestability.addToCostStack(methodTestability);
        return lastMethodTestability;
    }
}
