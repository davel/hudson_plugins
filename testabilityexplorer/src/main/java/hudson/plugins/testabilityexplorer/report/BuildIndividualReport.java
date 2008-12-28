package hudson.plugins.testabilityexplorer.report;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.maven.*;
import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;

import java.util.*;
import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.apache.commons.lang.StringUtils;

/**
 * Connects a {@link Statistic} with a {@link AbstractBuild}.
 *
 * @author reik.schatz
 */
public class BuildIndividualReport extends AbstractBuildReport<AbstractBuild<?, ?>> implements AggregatableAction
{
    private final CostDetailBuilder m_detailBuilder;

    public BuildIndividualReport(Collection<Statistic> results, ReportBuilder reportBuilder, CostDetailBuilder detailBuilder) {
        super(results, reportBuilder);
        m_detailBuilder = detailBuilder;
    }

    @Override
    public synchronized void setBuild(AbstractBuild<?, ?> build) {
        super.setBuild(build);
        if (this.getBuild() != null) {
            for (Statistic r : getResults()) {
                r.setOwner(this.getBuild());
            }
        }
    }

    /**
     * This will be called implicitly by Stapler framework, if there is a dynamic part in the regular link.<br />
     * Example: lets say your plugin is called <code>testability</code>. When requesting <code>http://hudson:8080/job/project/build/testability/foo</code>
     * this method gets called with <code>foo</code> as first parameter.
     *
     * @param link the dynamic part of the url after the plugin name
     * @param request StaplerRequest
     * @param response StaplerResponse
     * @return any object that you want to work with in .jelly files
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response)
    {
        return m_detailBuilder.buildDetail(link, getBuild(), getResults());
    }

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild mavenModuleSetBuild, Map<MavenModule, List<MavenBuild>> mavenModuleListMap)
    {
        return new BuildAggregatedReport(mavenModuleSetBuild, getResults(), getReportBuilder());
    }
}
