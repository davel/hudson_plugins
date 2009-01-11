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
    public BuildIndividualReport(Collection<Statistic> results, ReportBuilder reportBuilder, CostDetailBuilder detailBuilder)
    {
        super(results, reportBuilder, detailBuilder);
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

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild mavenModuleSetBuild, Map<MavenModule, List<MavenBuild>> mavenModuleListMap)
    {
        return new BuildAggregatedReport(mavenModuleSetBuild, getResults(), getReportBuilder(), getDetailBuilder());
    }
}
