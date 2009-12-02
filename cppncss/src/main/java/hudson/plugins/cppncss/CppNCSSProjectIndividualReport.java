package hudson.plugins.cppncss;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.plugins.helpers.AbstractProjectAction;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO javadoc.
 * 
 * @author Stephen Connolly
 * @since 08-Jan-2008 22:05:48
 */
public class CppNCSSProjectIndividualReport extends
		AbstractProjectReport<AbstractProject<?, ?>> implements
		ProminentProjectAction {
	private CppNCSSProjectFunctionIndividualReport cppFunction;
	private AbstractProject project;
	private Integer functionCcnViolationThreshold;
	private Integer functionNcssViolationThreshold;

	public CppNCSSProjectIndividualReport(AbstractProject<?, ?> project,
			Integer functionCcnViolationThreshold,
			Integer functionNcssViolationThreshold) {
		super(project, functionCcnViolationThreshold,
				functionNcssViolationThreshold);
		this.project = project;
		this.functionCcnViolationThreshold = functionCcnViolationThreshold;
		this.functionNcssViolationThreshold = functionNcssViolationThreshold;
	}

	protected Class<? extends AbstractBuildReport> getBuildActionClass() {
		return CppNCSSBuildIndividualReport.class;
	}

	@Override
	public AbstractProjectAction getDynamic(String name, StaplerRequest req,
			StaplerResponse rsp) {
		if (cppFunction == null) {
			cppFunction = new CppNCSSProjectFunctionIndividualReport(project,
					functionCcnViolationThreshold,
					functionNcssViolationThreshold);
		}
		super.getDynamic(name, req, rsp);
		if (name.length() >= 1) {
			cppFunction.setFileName(name);
			cppFunction.setFilereport(this);
			return cppFunction;
		} else {
			return this;
		}
	}

}
