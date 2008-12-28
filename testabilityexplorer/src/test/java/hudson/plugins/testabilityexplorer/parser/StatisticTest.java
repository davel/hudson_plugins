package hudson.plugins.testabilityexplorer.parser;

import org.apache.tools.ant.filters.StringInputStream;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.parser.selectors.DefaultConverterSelector;
import hudson.plugins.testabilityexplorer.parser.selectors.ConverterSelector;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

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

        MethodCost constructor = (MethodCost) methodCosts.toArray() [0];
        assertEquals(48, constructor.getCyclomatic());
        assertEquals(1, constructor.getGlobal());
        assertEquals(43, constructor.getLine());
        assertEquals(0, constructor.getLod());
        assertEquals("com.ongame.bo.bofraud.markup.pages.IpLookupResultsPage(org.apache.wicket.PageParameters)", constructor.getName());
        assertEquals(58, constructor.getOverall());
        assertEquals("", constructor.getReason());

        Collection<MethodCost> costDetails = constructor.getCostStack();
        assertEquals(40, costDetails.size());

        MethodCost costDetail = (MethodCost) costDetails.toArray() [0];
        assertEquals(1, costDetail.getCyclomatic());
        assertEquals(0, costDetail.getGlobal());
        assertEquals(2594, costDetail.getLine());
        assertEquals(0, costDetail.getLod());
        assertEquals("org.apache.wicket.Component setComponentBorder(org.apache.wicket.IComponentBorder)", costDetail.getName());
        assertEquals(1, costDetail.getOverall());
        assertEquals("implicit cost calling all setters", costDetail.getReason());
    }
}
