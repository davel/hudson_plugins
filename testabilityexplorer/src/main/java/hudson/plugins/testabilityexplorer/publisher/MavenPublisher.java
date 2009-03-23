package hudson.plugins.testabilityexplorer.publisher;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.model.Action;
import hudson.plugins.testabilityexplorer.PluginImpl;
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

/**
 * A {@link AbstractMavenReporterImpl} that will set up the plugin so that it can be used in M2
 * projects.
 *
 * @author reik.schatz
 */
public class MavenPublisher extends AbstractMavenReporterImpl {

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends MavenReporterDescriptor {

        private DescriptorImpl() {
            super(MavenPublisher.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        @Override
        public MavenReporter newInstance(StaplerRequest req, JSONObject formData)
                throws FormException {
            return req.bindJSON(MavenPublisher.class, formData);
        }

        @Override
        public String getHelpFile() {
            return "help";
        }

        @Override
        public String getConfigPage() {
            return getViewPage(MavenPublisher.class, "config.jelly");
        }
    }

    private final String m_reportFilenamePattern;

    private final int m_threshold;

    private final int m_perClassThreshold;

    private final boolean m_aggregateFiles;

    private final double m_weightFactor;

    @DataBoundConstructor
    public MavenPublisher(String reportFilenamePattern, String aggregateFiles,
            String weightFactor, String threshold, String perClassThreshold) {
        reportFilenamePattern.getClass();
        threshold.getClass();

        m_reportFilenamePattern = reportFilenamePattern;
        m_threshold = toInt(threshold, Integer.MAX_VALUE);
        m_perClassThreshold = toInt(perClassThreshold, Integer.MAX_VALUE);
        m_aggregateFiles = toBool(aggregateFiles, false);
        m_weightFactor = toDouble(weightFactor, DEFAULT_WEIGHT);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getProjectAction(MavenModule module) {
        return new ProjectIndividualReport(module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MavenReporterDescriptor getDescriptor() {
        return DESCRIPTOR;
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
     * Retunrs the current threshold for which the build will fail if the testability score is above
     * it.
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
}
