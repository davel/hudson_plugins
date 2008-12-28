package hudson.plugins.testabilityexplorer.parser.converters;

import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.TestabilityCost;
import org.xmlpull.v1.XmlPullParser;

import java.util.Collection;

/**
 * Converts a &lt;method&gt; XML element into a {@link MethodCost}.
 *
 * @author reik.schatz
 */
public class MethodElementConverter extends ElementConverter
{
    /**
     * Constructs a new {@link MethodCost}.
     *
     * @param xpp XmlPullParser
     * @param root the parent {@link CostSummary} root
     * @return MethodCost
     */
    public TestabilityCost construct(XmlPullParser xpp, CostSummary root)
    {
        String namespace = getNamespace();
        int cyclomatic = toInt(xpp.getAttributeValue(namespace, "cyclomatic"), -1);
        int global = toInt(xpp.getAttributeValue(namespace, "global"), -1);
        int line = toInt(xpp.getAttributeValue(namespace, "line"), -1);
        int lod = toInt(xpp.getAttributeValue(namespace, "lod"), -1);
        String name = xpp.getAttributeValue(namespace, "name");
        int overall = toInt(xpp.getAttributeValue(namespace, "overall"), -1);

        MethodCost methodTestability = new MethodCost(name, cyclomatic, global, line, lod, overall, null);

        Collection<ClassCost> costStack = root.getCostStack();
        int lastItemIndex = costStack.size() -1;    // will be >= 0
        ClassCost classTestability = (ClassCost) costStack.toArray() [lastItemIndex];
        classTestability.addToCostStack(methodTestability);
        return classTestability;
    }
}
