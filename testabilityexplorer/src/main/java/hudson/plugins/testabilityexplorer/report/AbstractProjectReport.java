package hudson.plugins.testabilityexplorer.report;

import hudson.model.AbstractProject;
import hudson.model.AbstractBuild;
import hudson.model.ProminentProjectAction;
import hudson.plugins.testabilityexplorer.helpers.AbstractProjectAction;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.PluginImpl;

import java.util.Collections;
import java.util.Collection;
import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * Base class for project reports.
 *
 * @author reik.schatz
 */
public abstract class AbstractProjectReport<T extends AbstractProject<?, ?>> extends AbstractProjectAction<T> implements ProminentProjectAction
{

    public AbstractProjectReport(T project)
    {
        super(project);
    }

    /** {@inheritDoc} */
    public String getIconFileName()
    {
        AbstractBuildReport action  = getApplicableBuildAction();
        return action != null ? PluginImpl.ICON_FILE_NAME : null;
    }

    /** {@inheritDoc} */
    public String getDisplayName()
    {
        AbstractBuildReport action  = getApplicableBuildAction();
        return action != null ? PluginImpl.DISPLAY_NAME : null;
    }

    /** {@inheritDoc} */
    public String getUrlName()
    {
        AbstractBuildReport action  = getApplicableBuildAction();
        return action != null ? PluginImpl.URL : null;
    }

    /** {@inheritDoc} */
    public String getSearchUrl()
    {
        return PluginImpl.URL;
    }

    public Collection<Statistic> getResults()
    {
        AbstractBuildReport action  = getApplicableBuildAction();
        return action != null ? action.getResults() : Collections.emptySet();
    }

    public int getTotals()
    {
        AbstractBuildReport action  = getApplicableBuildAction();
        return action != null ? action.getTotals() : 0;
    }

    /**
     * Returns the first applicable {@link AbstractBuildReport} action in any of the
     * previous build. May return {@code null}.
     *
     * @return AbstractBuildReport or {@code null}
     */
    protected AbstractBuildReport getApplicableBuildAction()
    {
        for (AbstractBuild<?, ?> build = getProject().getLastBuild(); build != null; build = build.getPreviousBuild())
        {
            final AbstractBuildReport action = build.getAction(getBuildActionClass());
            if (action != null)
            {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns a {@link AbstractBuildReport} for rendering.
     * @return AbstractBuildReport
     */
    protected abstract Class<? extends AbstractBuildReport> getBuildActionClass();

}
