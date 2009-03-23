package hudson.plugins.testabilityexplorer;

import org.testng.annotations.Test;


/**
 * Tests the {@link PluginImpl} class.
 *
 * @author reik.schatz
 */
public class PluginImplTest
{
    private static class DisabledPlugin extends PluginImpl
    {
        @Override
        protected void addPublisher()
        {
            // don't touch Hudson
        }
    }

    @Test(enabled=false)
    public void testPublisher() throws Exception
    {
        PluginImpl plugin = new DisabledPlugin();
        plugin.start();
    }
}
