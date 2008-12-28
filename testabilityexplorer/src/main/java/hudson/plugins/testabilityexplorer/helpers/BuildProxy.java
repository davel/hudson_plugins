package hudson.plugins.testabilityexplorer.helpers;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.Callable;

import java.util.ArrayList;
import java.util.List;

import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.CostDetailBuilder;

/**
 * The {@link BuildProxy} is proxying the the real build and enriching it with additional helper classes. These
 * helper classes such as {@link CostDetailBuilder} or {@link StatisticsParser} will be used in different processing
 * steps as the {@link BuildProxy} is passed around.
 *
 * @author reik.schatz
 */
public final class BuildProxy
{
    private final FilePath m_moduleRoot;
    private final StatisticsParser m_statisticsParser;
    private final CostDetailBuilder m_detailBuilder;
    private final ReportBuilder m_reportBuilder;
    private final List<AbstractBuildAction<AbstractBuild<?, ?>>> m_actions = new ArrayList<AbstractBuildAction<AbstractBuild<?, ?>>>();
    private Result m_result = null;

    public BuildProxy(FilePath moduleRoot, StatisticsParser statisticsParser, CostDetailBuilder detailBuilder, ReportBuilder reportBuilder)
    {
        m_moduleRoot = moduleRoot;
        m_statisticsParser = statisticsParser;
        m_detailBuilder = detailBuilder;
        m_reportBuilder = reportBuilder;
    }

    public boolean doPerform(ParseDelegate parseDelegate, AbstractBuild<?, ?> build, BuildListener listener)
    {
        Callable<BuildProxy, Exception> callableHelper = new BuildProxyCallableHelper(this, parseDelegate, listener);
        BuildProxy buildProxy = null;
        try
        {
            buildProxy = this.getModuleRoot().act(callableHelper);
            buildProxy.updateBuild(build);
        }
        catch (Throwable e)
        {
            if (buildProxy != null)
            {
                buildProxy.setResult(Result.FAILURE);
            }
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public FilePath getModuleRoot()
    {
        return m_moduleRoot;
    }

    public StatisticsParser getStatisticsParser()
    {
        return m_statisticsParser;
    }

    public CostDetailBuilder getDetailBuilder()
    {
        return m_detailBuilder;
    }

    public ReportBuilder getReportBuilder()
    {
        return m_reportBuilder;
    }

    public Result getResult()
    {
        return m_result;
    }

    public void setResult(Result result)
    {
        m_result = result;
    }

    public void updateBuild(AbstractBuild<?, ?> build)
    {
        // add actions
        for (AbstractBuildAction<AbstractBuild<?, ?>> action : m_actions)
        {
            if (!build.getActions().contains(action))
            {
                action.setBuild(build);
                build.getActions().add(action);
            }
        }

        // set result
        Result result = getResult();
        build.setResult(result == null ? Result.SUCCESS : result);
    }

    public void addAction(AbstractBuildAction<AbstractBuild<?, ?>> abstractBuildAction)
    {
        m_actions.add(abstractBuildAction);
    }
}
