package hudson.plugins.testabilityexplorer.parser;

import org.apache.tools.ant.filters.StringInputStream;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.parser.selectors.DefaultConverterSelector;
import hudson.plugins.testabilityexplorer.parser.selectors.ConverterSelector;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.model.AbstractBuild;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link Statistic} class.
 *
 * @author reik.schatz
 */
@Test
public class StatisticTest extends PluginBaseTest
{
    public void testParse() throws IOException, XmlPullParserException
    {
        InputStream inputStream = new StringInputStream(createReportXml());

        ConverterSelector converterSelector = new DefaultConverterSelector();
        StatisticsParser statisticsParser = new XmlStatisticsParser(converterSelector);
        Collection<Statistic> statistics = statisticsParser.parse(inputStream);
        assertEquals(1, statistics.size());

        Statistic statistic = (Statistic) statistics.toArray() [0];
        CostSummary summary = statistic.getCostSummary();
        assertNotNull(summary);

        assertEquals(6, summary.getExcellent());
        assertEquals(3, summary.getGood());
        assertEquals(0, summary.getNeedsWork());
        assertEquals(40, summary.getTotal());

        Collection<ClassCost> costStack = summary.getCostStack();
        assertFalse(costStack.isEmpty());
        assertEquals(9, costStack.size());

        ClassCost ipLookupResultsPage = (ClassCost) costStack.toArray() [0];
        assertEquals(59, ipLookupResultsPage.getCost());
        assertEquals("com.ongame.bo.bofraud.markup.pages.IpLookupResultsPage", ipLookupResultsPage.getName());

        Collection<MethodCost> methodCosts = ipLookupResultsPage.getCostStack();
        assertFalse(methodCosts.isEmpty());
        assertEquals(8, methodCosts.size());

        MethodCost method1 = (MethodCost) methodCosts.toArray() [0];
        assertEquals(50, method1.getCyclomatic());
        assertEquals(1, method1.getGlobal());
        assertEquals(76, method1.getLine());
        assertEquals(0, method1.getLod());
        assertEquals("void addIpInfo()", method1.getName());
        assertEquals(60, method1.getOverall());
        assertEquals("", method1.getReason());

        Collection<MethodCost> costDetails = method1.getCostStack();
        assertEquals(37, costDetails.size());

        MethodCost costDetail = (MethodCost) costDetails.toArray() [0];
        assertEquals(1, costDetail.getCyclomatic());
        assertEquals(1, costDetail.getGlobal());
        assertEquals(1511, costDetail.getLine());
        assertEquals(0, costDetail.getLod());
        assertEquals("void setFormComponentValuesFromCookies()", costDetail.getName());
        assertEquals(11, costDetail.getOverall());
        assertEquals("implicit cost calling all setters", costDetail.getReason());
    }

    public void testEqualsAndHashcode()
    {
        CostSummary costSummary = new CostSummary(1, 2, 3, 20);
        Statistic s1 = new ArrayList<Statistic>(createStatistics(false, costSummary)).get(0);

        assertFalse(s1.equals(null));
        assertFalse(s1.equals("foo"));
        assertFalse(s1.equals(costSummary));
        assertTrue(s1.equals(s1));
        assertTrue(s1.hashCode() == s1.hashCode());

        AbstractBuild<?, ?> build1 = mock(AbstractBuild.class);
        stub(build1.toString()).toReturn("Build 1");
        s1.setOwner(build1);

        assertFalse(s1.equals(null));
        assertFalse(s1.equals("foo"));
        assertFalse(s1.equals(costSummary));
        assertTrue(s1.equals(s1));
        assertTrue(s1.hashCode() == s1.hashCode());

        Statistic s2 = new ArrayList<Statistic>(createStatistics(false, costSummary)).get(0);
        assertFalse(s1.equals(s2));
        assertFalse(s1.hashCode() == s2.hashCode());
        assertFalse(s2.equals(s1));
        assertFalse(s2.hashCode() == s1.hashCode());

        s2.setOwner(build1);

        assertTrue(s1.equals(s2));
        assertTrue(s1.hashCode() == s2.hashCode());
        assertTrue(s2.equals(s1));
        assertTrue(s2.hashCode() == s1.hashCode());

        CostSummary costSummary2 = new CostSummary(1234, 25, 343, 230);
        Statistic s3 = new ArrayList<Statistic>(createStatistics(false, costSummary2)).get(0);
        s3.setOwner(build1);
        assertTrue(s2.equals(s3));
        assertTrue(s2.hashCode() == s3.hashCode());
        assertTrue(s1.equals(s3));
        assertTrue(s1.hashCode() == s3.hashCode());

        CostSummary costSummary3 = new CostSummary(34, 325, 3543, 2630);
        Statistic s4 = new ArrayList<Statistic>(createStatistics(false, costSummary3)).get(0);
        AbstractBuild<?, ?> build2 = mock(AbstractBuild.class);
        stub(build2.toString()).toReturn("Another Build");
        s4.setOwner(build2);

        assertFalse(s4.equals(s1));
        assertFalse(s4.hashCode() == s1.hashCode());
        assertFalse(s4.equals(s2));
        assertFalse(s4.hashCode() == s2.hashCode());
        assertFalse(s4.equals(s3));
        assertFalse(s4.hashCode() == s3.hashCode());
        assertTrue(s4.equals(s4));
        assertTrue(s4.hashCode() == s4.hashCode());
    }
}
