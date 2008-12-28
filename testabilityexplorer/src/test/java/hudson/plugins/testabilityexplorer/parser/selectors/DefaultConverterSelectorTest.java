package hudson.plugins.testabilityexplorer.parser.selectors;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import hudson.plugins.testabilityexplorer.parser.converters.*;

/**
 * Tests the DefaultConverterSelector.
 *
 * @author reik.schatz
 */
@Test
public class DefaultConverterSelectorTest
{
    public void testGetConverter()
    {
        DefaultConverterSelector defaultConverterSelector = new DefaultConverterSelector();
        assertNull(defaultConverterSelector.getConverter(null));
        assertNull(defaultConverterSelector.getConverter(""));

        ElementConverter testabilityConverter = defaultConverterSelector.getConverter(DefaultConverterSelector.TAG_TESTABILITY);
        assertNotNull(testabilityConverter);
        assertTrue(testabilityConverter instanceof TestabilityElementConverter);

        ElementConverter classConverter = defaultConverterSelector.getConverter(DefaultConverterSelector.TAG_CLASS);
        assertNotNull(classConverter);
        assertTrue(classConverter instanceof ClassElementConverter);

        ElementConverter methodConverter = defaultConverterSelector.getConverter(DefaultConverterSelector.TAG_METHOD);
        assertNotNull(methodConverter);
        assertTrue(methodConverter instanceof MethodElementConverter);

        ElementConverter costConverter = defaultConverterSelector.getConverter(DefaultConverterSelector.TAG_COST);
        assertNotNull(costConverter);
        assertTrue(costConverter instanceof CostElementConverter);
    }
}
