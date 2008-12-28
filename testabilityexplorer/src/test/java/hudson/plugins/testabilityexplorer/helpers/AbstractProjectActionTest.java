package hudson.plugins.testabilityexplorer.helpers;

import hudson.plugins.testabilityexplorer.PluginBaseTest;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

import java.util.GregorianCalendar;

/**
 * Tests AbstractProjectAction.
 *
 * @author reik.schatz
 */
@Test
public class AbstractProjectActionTest extends PluginBaseTest
{
    public void testGraphMethods()
    {
        AbstractProjectAction abstractProjectAction = createAbstractProjectAction();
        assertFalse(abstractProjectAction.isFloatingBoxActive());
        assertFalse(abstractProjectAction.isGraphActive());
    }

    private AbstractProjectAction createAbstractProjectAction()
    {
        AbstractProject<?, ?> project = mock(AbstractProject.class);

        return new AbstractProjectAction(project)
        {
            public String getDisplayName()
            {
                return "";
            }

            public String getSearchUrl()
            {
                return "";
            }
        };
    }
}
