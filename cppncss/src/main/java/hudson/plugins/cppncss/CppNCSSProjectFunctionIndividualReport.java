/**
 * 
 */
package hudson.plugins.cppncss;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.plugins.helpers.AbstractProjectAction;

/**
 * @author zjianguo
 * 
 */
public class CppNCSSProjectFunctionIndividualReport extends
		CppNCSSProjectIndividualReport implements ProminentProjectAction {

	public CppNCSSProjectFunctionIndividualReport(AbstractProject<?, ?> project,
			Integer functionCcnViolationThreshold,
			Integer functionNcssViolationThreshold) {
		super(project, functionCcnViolationThreshold,
				functionNcssViolationThreshold);
	}

	private String fileName;
	private AbstractProjectAction filereport;

	public AbstractProjectAction getFilereport() {
		return filereport;
	}

	public void setFilereport(AbstractProjectAction filereport) {
		this.filereport = filereport;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public AbstractProjectAction getDynamic(String name, StaplerRequest req,
			StaplerResponse rsp) {
		if (name.length() < 1) {
			return this.filereport;
		} else {
			return this;
		}
	}

	@Override
	public String getDisplayName() {
//		return fileName.substring(fileName.lastIndexOf("/")).substring(
//				fileName.lastIndexOf("\\"));
		return fileName;
	}
}
