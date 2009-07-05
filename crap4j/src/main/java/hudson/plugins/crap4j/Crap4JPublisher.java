package hudson.plugins.crap4j;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.crap4j.calculation.HealthBuilder;
import hudson.plugins.crap4j.model.CrapReportMerger;
import hudson.plugins.crap4j.model.IMethodCrap;
import hudson.plugins.crap4j.model.MethodCrapBean;
import hudson.plugins.crap4j.model.ProjectCrapBean;
import hudson.plugins.crap4j.util.FoundFile;
import hudson.plugins.crap4j.util.ReportFilesFinder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.schneide.crap4j.reader.ReportReader;
import com.schneide.crap4j.reader.model.ICrapReport;
import com.schneide.crap4j.reader.model.IMethodCrapData;

public class Crap4JPublisher extends Recorder {

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
	public BuildStepDescriptor<Publisher> getDescriptor() {
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
        ProjectCrapBean previousCrap = getPreviousProjectCrapBean(build);
        ProjectCrapBean reportBean = createCurrentProjectCrapBean(logger,
				reports, previousCrap);
        build.getActions().add(new Crap4JBuildAction(
        		build,
        		new CrapBuildResult(build, reportBean),
        		this.healthBuilder));
		return true;
	}

	private ProjectCrapBean getPreviousProjectCrapBean(AbstractBuild<?, ?> build) {
        CrapBuildResult previousResult = CrapBuildResult.getPrevious(build);
        if (null != previousResult) {
        	return previousResult.getResultData();
        }
		return null;
	}

	private ProjectCrapBean createCurrentProjectCrapBean(PrintStream logger,
			FoundFile[] reports, ProjectCrapBean previousCrap)
			throws UnsupportedEncodingException, IOException {
		ProjectCrapBean[] currentBeans = loadProjectCrapBeans(logger, reports, previousCrap);
		if (1 == currentBeans.length) {
			return currentBeans[0];
		}
		CrapReportMerger merger = new CrapReportMerger();
		return merger.mergeReports(previousCrap, currentBeans);
	}
	
	private ProjectCrapBean[] loadProjectCrapBeans(PrintStream logger,
			FoundFile[] reports, ProjectCrapBean previousCrap)
			throws UnsupportedEncodingException, IOException {
		List<ProjectCrapBean> result = new ArrayList<ProjectCrapBean>();
		for (FoundFile currentReportFile : reports) {
			Reader reportReader = new BufferedReader(
	        		new InputStreamReader(
	        				currentReportFile.getFile().read(),
	        				currentReportFile.getEncoding()));
	        ReportReader parser = new ReportReader(reportReader);
	        ICrapReport report = parser.parseData();
	        ProjectCrapBean reportBean = new ProjectCrapBean(
	        		previousCrap,
	        		report.getStatistics(),
	        		convertToMethodCrap(report.getDetails().getMethodCrapManager().getCrapData()));
	        log(logger, "Got a report bean with " + reportBean.getCrapMethodCount() + " crap methods out of " + reportBean.getMethodCount() + " methods.");
	        result.add(reportBean);
		}
		return result.toArray(new ProjectCrapBean[result.size()]);
	}

	private IMethodCrap[] convertToMethodCrap(Collection<IMethodCrapData> crapData) {
		IMethodCrap[] result = new IMethodCrap[crapData.size()];
		Iterator<IMethodCrapData> dataIterator = crapData.iterator();
		int index = 0;
		while (dataIterator.hasNext()) {
			result[index] = new MethodCrapBean(dataIterator.next());
			index++;
		}
		return result;
	}

	public String getReportPattern() {
		return this.reportPattern;
	}

	public String getHealthThreshold() {
		return String.valueOf(this.healthBuilder.getThreshold());
	}
}
