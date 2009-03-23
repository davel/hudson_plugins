package hudson.plugins.testabilityexplorer.publisher;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.plugins.testabilityexplorer.helpers.ParseDelegate;
import hudson.plugins.testabilityexplorer.helpers.ReportParseDelegate;
import hudson.plugins.testabilityexplorer.parser.StatisticsParser;
import hudson.plugins.testabilityexplorer.parser.XmlStatisticsParser;
import hudson.plugins.testabilityexplorer.parser.selectors.DefaultConverterSelector;
import hudson.plugins.testabilityexplorer.report.CostDetailBuilder;
import hudson.plugins.testabilityexplorer.report.ProjectIndividualReport;
import hudson.plugins.testabilityexplorer.report.charts.ChartBuilder;
import hudson.plugins.testabilityexplorer.report.charts.TestabilityChartBuilder;
import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
import hudson.tasks.Publisher;

/**
 * Main publisher class. Expects two values from the GUI which are mapped by the Stapler framework.
 *
 * @author reik.schatz
 */
public class FreestylePublisher extends AbstractPublisherImpl {

    public static final Descriptor DESCRIPTOR = new TestabilityExplorerDescriptor();

    private final String m_reportFilenamePattern;

    private final int m_threshold;

    private final int m_perClassThreshold;

    private final boolean m_aggregateFiles;

    private final double m_weightFactor;

    @DataBoundConstructor
    public FreestylePublisher(String reportFilenamePattern, String aggregateFiles,
            String weightFactor, String threshold, String perClassThreshold) {
        m_reportFilenamePattern = reportFilenamePattern;
        m_threshold = toInt(threshold, Integer.MAX_VALUE);
        m_perClassThreshold = toInt(perClassThreshold, Integer.MAX_VALUE);
        m_aggregateFiles = toBool(aggregateFiles, false);
        m_weightFactor = toDouble(weightFactor, DEFAULT_WEIGHT);
    }

    public ParseDelegate newParseDelegate() {
        return new ReportParseDelegate(getReportFilenamePattern(), getAggregateFiles(),
                getWeightFactor(), getThreshold(), getPerClassThreshold());
    }

    public StatisticsParser newStatisticsParser() {
        return new XmlStatisticsParser(new DefaultConverterSelector());
    }

    public CostDetailBuilder newDetailBuilder() {
        return new CostDetailBuilder();
    }

    public ReportBuilder newReportBuilder() {
        ChartBuilder chartBuilder = new TestabilityChartBuilder();
        return new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
    }

    /**
     * Returns the current file pattern to the report files.
     *
     * @return String
     */
    public String getReportFilenamePattern() {
        return m_reportFilenamePattern;
    }

    /**
     * Returns the current threshold for which the build will become unstable if the testability
     * score is above it.
     *
     * @return int
     */
    public int getThreshold() {
        return m_threshold;
    }

    public boolean getAggregateFiles() {
        return m_aggregateFiles;
    }

    public double getWeightFactor() {
        return m_weightFactor;
    }

    /**
     * Returns the current threshold, for which the build will become unstable if the testability
     * score is above it, on a per class basis.
     *
     * @return int
     */
    public int getPerClassThreshold() {
        return m_perClassThreshold;
    }

    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Action getProjectAction(Project project) {
        return new ProjectIndividualReport(project);
    }
}
