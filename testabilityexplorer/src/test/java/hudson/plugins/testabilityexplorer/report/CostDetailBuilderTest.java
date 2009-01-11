package hudson.plugins.testabilityexplorer.report;

import org.testng.annotations.Test;
import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.report.costs.ClassCost;
import hudson.plugins.testabilityexplorer.report.costs.MethodCost;
import hudson.plugins.testabilityexplorer.report.detail.ClassCostDetail;
import hudson.plugins.testabilityexplorer.report.detail.MethodCostDetail;
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

        Object o = costDetailBuilder.buildDetail("class." + classCost.getName(), "", build, statistics);
        assertTrue(o instanceof ClassCostDetail);

        ClassCostDetail classCostDetail = (ClassCostDetail) o;
        ClassCost cost = classCostDetail.getClassCost();
        assertTrue(classCost == cost);
        assertEquals(classCostDetail.getDisplayName(), "hudson.plugins.testabilityexplorer.testabilityexplorer.PluginBaseTest");
        assertTrue(classCostDetail.toString().contains("ClassCost: Name: hudson.plugins.testabilityexplorer.testabilityexplorer.PluginBaseTest, Cost: 20"));

        AbstractBuild<?, ?> owner = classCostDetail.getOwner();
        assertTrue(build == owner);

    }

    public void testFindMatch()
    {
        CostDetailBuilder costDetailBuilder = new CostDetailBuilder();
        String s1 = costDetailBuilder.findMatch("class.Foo:line.65");
        assertNotNull(s1);
        assertEquals(s1, "65");
        assertNull(costDetailBuilder.findMatch("class.Foo:line65"));
        assertNull(costDetailBuilder.findMatch("class.Foo:inline.65"));
        assertNotNull(costDetailBuilder.findMatch("class.Foo:line.65/"));
    }

    public void testLookupMethodCost()
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
        classCost.addToCostStack(new MethodCost("foo", 1, 1, 67, 1, 1, "foo"));

        Object o1 = costDetailBuilder.buildDetail("class." + classCost.getName(), "", build, statistics);
        assertTrue(o1 instanceof ClassCostDetail);

        Object o2 = costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.56", build, statistics);
        assertTrue(o2 instanceof MethodCostDetail);

        Object o3 = costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.67", build, statistics);
        assertTrue(o3 instanceof MethodCostDetail);

        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line67", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line1", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.11", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.116895", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ".133:line", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.56.fldjlfsj", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.56.", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":line.56:line.5", build, statistics) instanceof ClassCostDetail);
        assertTrue(costDetailBuilder.buildDetail("class." + classCost.getName(), "class." + classCost.getName() + ":inline.56", build, statistics) instanceof ClassCostDetail);
    }
}
