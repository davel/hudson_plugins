package hudson.plugins.analysis.collector;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.graph.EmptyGraph;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.graph.PriorityGraph;
import net.sf.json.JSONObject;

import org.junit.Test;
import org.mortbay.util.ajax.JSON;

import com.google.common.collect.Sets;

/**
 * Tests the class {@link AnalysisGraphConfiguration}.
 *
 * @author Ulli Hafner
 */
public class AnalysisGraphConfigurationTest {
    /**
     * Asserts that the provided initialization value is correctly rejected and
     * the configuration is initialized by default values.
     *
     * @param initializationValue
     *            initialization value
     */
    private void assertInvalidInitializationValue(final String initializationValue) {
        GraphConfiguration configuration = createConfigurationUnderTest();

        assertFalse("Invalid configuration accepted.", configuration.initializeFrom(initializationValue));
        assertTrue("Invalid configuration state.", configuration.isDefault());
    }

    /**
     * Creates the configuration under test.
     *
     * @return the configuration under test
     */
    private AnalysisGraphConfiguration createConfigurationUnderTest() {
        return new AnalysisGraphConfiguration(Sets.newHashSet(new PriorityGraph(), new OriginGraph(), new EmptyGraph()));
    }

    /**
     * Ensures that invalid string values are rejected.
     */
    @Test
    public void testInvalidConfiguations() {
        assertInvalidInitializationValue("50!50!12!13!PRIORITY");
        assertInvalidInitializationValue("50!50!12!13!ORIGIN");
        assertInvalidInitializationValue("50!50!12!13!ORIGIN!2");
        assertInvalidInitializationValue("50!50!12!13!ORIGIN!q");
    }

    /**
     * Ensures that a valid configuration is correctly parsed.
     */
    @Test
    public void testValidConfiguations() {
        AnalysisGraphConfiguration configuration = createConfigurationUnderTest();

        assertTrue("Valid configuration rejected", configuration.initializeFrom("50!50!12!13!ORIGIN!0"));
        assertFalse("Wrong value of deactivate property", configuration.canDeacticateOtherTrendGraphs());

        AnalysisGraphConfiguration other = createConfigurationUnderTest();
        assertTrue("Valid configuration rejected", other.initializeFrom(configuration.serializeToString()));
        assertEquals("Wrong serialization", configuration, other);

        assertTrue("Valid configuration rejected", configuration.initializeFrom("50!50!12!13!ORIGIN!1"));
        assertTrue("Wrong value of deactivate property", configuration.canDeacticateOtherTrendGraphs());
    }

    /**
     * Ensures that a valid JSON configuration is correctly parsed.
     */
    @Test
    public void testValidJSONConfiguations() {
        Object enabled = JSON.parse("{\"\":\"\",\"buildCountString\":\"\",\"canDeacticateOtherTrendGraphs\":true,\"dayCountString\":\"\",\"graphType\":\"ORIGIN\",\"height\":\"200\",\"width\":\"500\"}");
        JSONObject jsonObject = JSONObject.fromObject(enabled);

        AnalysisGraphConfiguration configuration = createConfigurationUnderTest();

        assertTrue("Valid configuration rejected", configuration.initializeFrom(jsonObject));
        assertTrue("Wrong value of deactivate property", configuration.canDeacticateOtherTrendGraphs());

        Object disabled = JSON.parse("{\"\":\"\",\"buildCountString\":\"\",\"canDeacticateOtherTrendGraphs\":false,\"dayCountString\":\"\",\"graphType\":\"ORIGIN\",\"height\":\"200\",\"width\":\"500\"}");
        jsonObject = JSONObject.fromObject(disabled);

        assertTrue("Valid configuration rejected", configuration.initializeFrom(jsonObject));
        assertFalse("Wrong value of deactivate property", configuration.canDeacticateOtherTrendGraphs());
    }
}

