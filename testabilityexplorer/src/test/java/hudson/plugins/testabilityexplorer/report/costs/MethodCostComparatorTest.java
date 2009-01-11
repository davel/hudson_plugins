package hudson.plugins.testabilityexplorer.report.costs;

import hudson.plugins.testabilityexplorer.PluginBaseTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Tests the MethodCostComparator class.
 *
 * @author reik.schatz
 */
@Test
public class MethodCostComparatorTest extends PluginBaseTest
{
    public void testSorting()
    {
        List<MethodCost> methods1 = new ArrayList<MethodCost>();
        methods1.add(new MethodCost("first", 5, 25, 56, 0, 20, ""));
        
        Collections.sort(methods1, MethodCostComparator.getInstance());
        assertOrdering(methods1);

        methods1.add(new MethodCost("second", 5, 25, 56, 0, 29, ""));
        Collections.sort(methods1, MethodCostComparator.getInstance());
        assertOrdering(methods1);

        Random randomGenerator = new Random();
        for (int i = 0; i < 100; i++)
        {
            methods1.add(new MethodCost("more" + i, 5, 25, 56, 0, randomGenerator.nextInt(), ""));
        }
    }

    private void assertOrdering(List<MethodCost> methods)
    {
        Integer overall = null;
        for (MethodCost method : methods)
        {
            if (overall != null)
            {
                assertTrue(overall >= method.getOverall());
            }
            overall = method.getOverall();
        }
    }
}
