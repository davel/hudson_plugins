package hudson.plugins.testabilityexplorer.helpers;

import hudson.model.AbstractBuild;
import hudson.model.HealthReportingAction;

/**
 * Abstract {@link HealthReportingAction} that will contain a reference to the build.
 *
 * @author reik.schatz
 */
public abstract class AbstractBuildAction<T extends AbstractBuild<?, ?>> implements HealthReportingAction
{
    private T m_build = null;

    protected AbstractBuildAction()
    {
    }

    public synchronized T getBuild()
    {
        return m_build;
    }

    public synchronized void setBuild(T build)
    {
        if (this.m_build == null && this.m_build != build)
        {
            this.m_build = build;
        }
    }

    /**
     * Enable's the floating box on the build summary page.
     * @return Boolean
     */
    public boolean isFloatingBoxActive()
    {
        return false;
    }

    /**
     * Activate the graph inside the floating box.
     * @return Boolean
     */
    public boolean isGraphActive()
    {
        return false;
    }

    /**
     * Title that will be displayed above the graph.
     * @return String
     */
    public String getGraphName()
    {
        return getDisplayName();
    }

    /**
     * Controls the summary text to display beside the build report icon on the build summary page.
     * @return String
     */
    public String getSummary()
    {
        return "";
    }
}


