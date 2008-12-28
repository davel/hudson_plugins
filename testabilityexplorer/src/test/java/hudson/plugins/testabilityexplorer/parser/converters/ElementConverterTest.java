package hudson.plugins.testabilityexplorer.parser.converters;

import hudson.plugins.testabilityexplorer.report.costs.TestabilityCost;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import org.xmlpull.v1.XmlPullParser;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests the toInt method in the ElementConverter class.
 *
 * @author reik.schatz
 */
@Test
public class ElementConverterTest
{
    public void testToInt()
    {
        ElementConverter elementConverter = new ElementConverter()
        {
            public TestabilityCost construct(XmlPullParser xpp, CostSummary root)
            {
                return null;
            }
        };
        assertEquals(elementConverter.toInt("1", 0), 1);
        assertEquals(elementConverter.toInt("x", 0), 0);
        assertEquals(elementConverter.toInt("", 0), 0);
        assertEquals(elementConverter.toInt("9999", 0), 9999);
        assertEquals(elementConverter.toInt("999999999999999999999999999999", 0), 0);
    }
}
