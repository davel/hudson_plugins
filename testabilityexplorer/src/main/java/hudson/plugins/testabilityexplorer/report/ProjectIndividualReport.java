package hudson.plugins.testabilityexplorer.report;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Wraps an individual project report for the Testability Explorer.
 *
 * @author reik.schatz
 */
public class ProjectIndividualReport extends AbstractProjectReport<AbstractProject<?, ?>> implements ProminentProjectAction
{
    public ProjectIndividualReport(AbstractProject<?, ?> project) {
        super(project);
    }

    @Override
    protected Class<? extends AbstractBuildReport> getBuildActionClass() {
        return BuildIndividualReport.class;
    }

    @Override
    public boolean isFloatingBoxActive()
    {
        return true;
    }

    @Override
    public boolean isGraphActive()
    {
        return true;
    }
}
