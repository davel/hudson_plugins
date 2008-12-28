package hudson.plugins.testabilityexplorer.publisher;

import hudson.plugins.testabilityexplorer.helpers.BuildProxy;
import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Publisher;

import java.io.IOException;

/**
 * Performs the actual work to go into the reports, do some parsing and flag the build
 * as being success or not. The {@link Publisher} uses the given {@link BuildProxy} to perform
 * the work for him using whatever is implemented as {@link hudson.plugins.testabilityexplorer.helpers.ParseDelegate}, {@link StatisticsParser} and
 * {@link ReportBuilder}.
 *
 * @author reik.schatz
 */
public abstract class AbstractPublisherImpl extends Publisher implements ExtractAndBuildDelegate
{
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
    {
        AbstractProject project = getProject(build);
        BuildProxy buildProxy = new BuildProxy(
                project.getModuleRoot(),
                newStatisticsParser(),
                newDetailBuilder(),
                newReportBuilder()
        );
        return buildProxy.doPerform(newParseDelegate(), build, listener);
    }

    protected AbstractProject getProject(AbstractBuild build)
    {
        return build.getProject();
    }

    @Override
    public boolean prebuild(AbstractBuild build, BuildListener listener)
    {
        return true;
    }
}
