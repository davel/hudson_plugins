package hudson.plugins.testabilityexplorer.helpers;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.AbstractBuild;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import hudson.plugins.testabilityexplorer.report.BuildIndividualReport;
import hudson.plugins.testabilityexplorer.report.AbstractBuildReport;

/**
 * A abstract {@link Actionable} that will contain a reference to the current project. Subclasses
 * need to overwrite methods like {@link AbstractProjectAction#isFloatingBoxActive()} to customize
 * displaying of the trend graphs.
 *
 * @author reik.schatz
 */
abstract public class AbstractProjectAction<T extends AbstractProject<?, ?>> extends Actionable {

    private final T m_project;

    protected AbstractProjectAction(T project)
    {
        m_project = project;
    }

    public T getProject()
    {
        return m_project;
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
        return "Testabilty Trend Report";
    }

    /**
     * Will use the specified {@link StaplerResponse} to render a trend graph. If no lost finished build
     * is found or that last finished build does not contain a {@link AbstractBuildReport} action, the
     * status on the StaplerResponse will be set to {@link HttpServletResponse#SC_NOT_FOUND}
     *
     * @param request a StaplerRequest
     * @param response a StaplerResponse
     * @throws IOException if a problem with the StaplerResponse occurs
     */
    public void doTrend(final StaplerRequest request, final StaplerResponse response) throws IOException
    {
        AbstractBuildReport action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            action.doTrendGraph(request, response, 100);
        }
    }

    /**
     * Returns the {@link AbstractBuildReport} action from the last finished build or {@code null}.
     * @return AbstractBuildReport or {@code null}
     */
    private AbstractBuildReport getLastAction()
    {
        AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        if (lastBuild != null) {
            return lastBuild.getAction(AbstractBuildReport.class);
        }
        return null;
    }

    /**
     * Returns the last finished build that contains a action of type TestabilityExplorerBuildIndividualReport.class.
     *
     * @return the last finished build or <code>null</code> if there is no such build
     */
    private AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = m_project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(AbstractBuildReport.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }
}
