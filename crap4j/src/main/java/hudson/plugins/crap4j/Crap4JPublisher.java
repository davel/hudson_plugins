package hudson.plugins.crap4j;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.plugins.crap4j.calculation.HealthBuilder;
import hudson.plugins.crap4j.model.ProjectCrapBean;
import hudson.plugins.crap4j.util.FoundFile;
import hudson.plugins.crap4j.util.ReportFilesFinder;
import hudson.tasks.Publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.schneide.crap4j.reader.ReportReader;
import com.schneide.crap4j.reader.model.ICrapReport;

public class Crap4JPublisher extends Publisher {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(Crap4JPublisher.class.getName());

    public static final Crap4JPluginDescriptor DESCRIPTOR = new Crap4JPluginDescriptor();

	private final String reportPattern;
	private HealthBuilder healthBuilder;

	/**
	 * @param reportPattern
	 * @stapler-constructor
	 */
	public Crap4JPublisher(String reportPattern,
			String healthThreshold) {
		super();
		this.reportPattern = reportPattern;
		this.healthBuilder = getHealthBuilderFor(healthThreshold);
	}

	private HealthBuilder getHealthBuilderFor(String healthThreshold) {
		if (null == healthThreshold) {
			return DESCRIPTOR.getHealthBuilder();
		}
		try {
			return new HealthBuilder(Double.parseDouble(healthThreshold));
		} catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Could not parse health threshold representation to a number: " + healthThreshold, e);
			return DESCRIPTOR.getHealthBuilder();
		} catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Not a valid health threshold: " + healthThreshold, e);
			return DESCRIPTOR.getHealthBuilder();
		}
	}

	//@Override
	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new Crap4JProjectAction(project);
	}

    protected void log(final PrintStream logger, final String message) {
        logger.println("[CRAP4J] " + message);
    }

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        log(logger, "Collecting Crap4J analysis files...");

        log(logger, "Searching for report files within " + this.reportPattern);
        log(logger, "Using the new FileSetBuilder");
        
        ReportFilesFinder finder = new ReportFilesFinder(this.reportPattern);
        FoundFile[] reports = build.getProject().getWorkspace().act(finder);
        if (0 == reports.length) {
            log(logger, "No crap4j report files were found. Configuration error?");
            return false;
        }
        Reader reportReader = new BufferedReader(
        		new InputStreamReader(
        				reports[0].getFile().read(),
        				reports[0].getEncoding()));
        ReportReader parser = new ReportReader(reportReader);
        ICrapReport report = parser.parseData();

        ProjectCrapBean previousCrap = null;
        CrapBuildResult previousResult = CrapBuildResult.getPrevious(build);
        if (null != previousResult) {
        	previousCrap = previousResult.getResultData();
        }
        ProjectCrapBean reportBean = new ProjectCrapBean(
        		previousCrap,
        		report.getStatistics(),
        		report.getDetails().getMethodCrapManager().getAllCrapData());
        log(logger, "Got a report bean with " + reportBean.getCrapMethodCount() + " crap methods out of " + reportBean.getMethodCount() + " methods.");
        build.getActions().add(new Crap4JBuildAction(
        		build,
        		new CrapBuildResult(build, reportBean),
        		this.healthBuilder));
		return true;
	}

	public String getReportPattern() {
		return this.reportPattern;
	}

	public String getHealthThreshold() {
		return String.valueOf(this.healthBuilder.getThreshold());
	}
}
