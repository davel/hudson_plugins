package hudson.plugins.testabilityexplorer.report;

import org.testng.annotations.Test;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.detail.ClassCostDetail;
import hudson.model.AbstractBuild;

import java.util.GregorianCalendar;
import java.util.Collection;

import static org.testng.Assert.*;

/**
 * Tests the CostDetailBuilder.
 */
@Test
public class CostDetailBuilderTest extends PluginBaseTest
{
    public void testBuildDetail()
    {
        CostDetailBuilder costDetailBuilder = new CostDetailBuilder();
        AbstractBuild<?,?> build = createBuild(11, GregorianCalendar.getInstance());
        Collection<Statistic> statistics = createStatistics();

        ClassCost classCost = null;
        for (Statistic statistic : statistics)
        {
            CostSummary summary = statistic.getCostSummary();
            Collection<ClassCost> costStack = summary.getCostStack();
            for (ClassCost cost : costStack)
            {
                classCost = cost;
                break;
            }
        }
        assertNotNull(classCost);

        Object o = costDetailBuilder.buildDetail("class." + classCost.getName(), build, statistics);
        assertTrue(o instanceof ClassCostDetail);

        ClassCostDetail classCostDetail = (ClassCostDetail) o;
        ClassCost cost = classCostDetail.getClassCost();
        assertTrue(classCost == cost);
        assertEquals(classCostDetail.getDisplayName(), "hudson.plugins.testabilityexplorer.testabilityexplorer.PluginBaseTest");
        assertTrue(classCostDetail.toString().contains("ClassCost: Name: hudson.plugins.testabilityexplorer.testabilityexplorer.PluginBaseTest, Cost: 20"));

        AbstractBuild<?, ?> owner = classCostDetail.getOwner();
        assertTrue(build == owner);

    }
}
