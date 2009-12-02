package hudson.plugins.cppncss;

import hudson.model.AbstractBuild;
import hudson.plugins.cppncss.parser.StatisticsResult;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CppNcssBuildFunctionIndividualReport<BUILD extends AbstractBuild<?, ?>>
		extends CppNCSSBuildIndividualReport {

	public CppNcssBuildFunctionIndividualReport(StatisticsResult results,
			Integer functionCcnViolationThreshold,
			Integer functionNcssViolationThreshold) {
		super(results, functionCcnViolationThreshold,
				functionNcssViolationThreshold);
	}

	private String fileName;
	private AbstractBuildReport filereport;

	public AbstractBuildReport getFilereport() {
		return filereport;
	}

	public void setFilereport(AbstractBuildReport filereport) {
		this.filereport = filereport;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public AbstractBuildReport getDynamic(String name, StaplerRequest req,
			StaplerResponse rsp) {
		if (name.length() < 1) {
			return this.filereport;
		} else {
			return this;
		}
	}

	@Override
	public String getDisplayName() {
		return fileName;
	}

}
