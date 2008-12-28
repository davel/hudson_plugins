package hudson.plugins.testabilityexplorer.parser.converters;

import org.xmlpull.v1.XmlPullParser;
import hudson.plugins.testabilityexplorer.report.costs.TestabilityCost;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.utils.TypeConverterUtil;

/**
 * Constructs {@link TestabilityCost} objects.
 *
 * @author reik.schatz
 */
public abstract class ElementConverter
{
    public abstract TestabilityCost construct(XmlPullParser xpp, CostSummary root);

    protected String getNamespace()
    {
        return null;
    }

    protected int toInt(String value, int defaultValue)
    {
        return TypeConverterUtil.toInt(value,defaultValue);
    }
}
