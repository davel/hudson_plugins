package hudson.plugins.clearcase;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.clearcase.action.ChangeLogAction;
import hudson.plugins.clearcase.action.CheckOutAction;
import hudson.plugins.clearcase.action.DefaultPollAction;
import hudson.plugins.clearcase.action.PollAction;
import hudson.plugins.clearcase.action.SaveChangeLogAction;
import hudson.plugins.clearcase.action.UcmDynamicCheckoutAction;
import hudson.plugins.clearcase.action.UcmSnapshotCheckoutAction;
import hudson.plugins.clearcase.history.Filter;
import hudson.plugins.clearcase.ucm.UcmChangeLogAction;
import hudson.plugins.clearcase.ucm.UcmChangeLogParser;
import hudson.plugins.clearcase.ucm.UcmPollAction;
import hudson.plugins.clearcase.ucm.UcmSaveChangeLogAction;
import hudson.plugins.clearcase.util.BuildVariableResolver;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.VariableResolver;

import java.io.IOException;

import java.util.List;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * SCM for ClearCaseUCM. This SCM will create a UCM view from a stream and apply
 * a list of load rules to it.
 */
public class ClearCaseUcmSCM extends AbstractClearCaseScm {

	private final String stream;
	private final String loadRules;
	private boolean useDynamicView;
	private String viewDrive;

	@DataBoundConstructor
	public ClearCaseUcmSCM(String stream, String loadrules, String viewname,
			boolean usedynamicview, String viewdrive,
			String mkviewoptionalparam, boolean filterOutDestroySubBranchEvent,
			boolean useUpdate) {
		super(viewname, mkviewoptionalparam, filterOutDestroySubBranchEvent,
				useUpdate);
		this.stream = shortenStreamName(stream);
		this.loadRules = loadrules;
		this.useDynamicView = usedynamicview;
		this.viewDrive = viewdrive;

	}

	/**
	 * Return the load rules for the UCM view.
	 * 
	 * @return string containing the load rules.
	 */
	public String getLoadRules() {
		return loadRules;
	}

	/**
	 * Return the stream that is used to create the UCM view.
	 * 
	 * @return string containing the stream selector.
	 */
	public String getStream() {
		return stream;
	}

	public boolean isUseDynamicView() {
		return useDynamicView;
	}

	public String getViewDrive() {
		return viewDrive;
	}

	@Override
	public ClearCaseUcmScmDescriptor getDescriptor() {
		return PluginImpl.UCM_DESCRIPTOR;
	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		return new UcmChangeLogParser();
	}

	@Override
	public String[] getBranchNames() {
		String branch = stream;
		if (stream.contains("@")) {
			branch = stream.substring(0, stream.indexOf("@"));
		}
		return new String[] { branch };
	}

	@Override
	public String[] getViewPaths(FilePath viewPath) throws IOException,
			InterruptedException {
		String[] rules = loadRules.split("\n");
		for (int i = 0; i < rules.length; i++) {
			String rule = rules[i];
			// Remove "\\", "\" or "/" from the load rule. (bug#1706) Only if
			// the view is not dynamic
			// the user normally enters a load rule beginning with those chars
			while (!useDynamicView
					&& (rule.startsWith("\\") || rule.startsWith("/"))) {
				rule = rule.substring(1);
			}
			rules[i] = rule;
		}
		return rules;
	}

	@Override
	protected CheckOutAction createCheckOutAction(
			VariableResolver variableResolver, ClearToolLauncher launcher) {
		CheckOutAction action;
		if (useDynamicView) {
			action = new UcmDynamicCheckoutAction(createClearTool(
					variableResolver, launcher), getStream());
		} else {
			action = new UcmSnapshotCheckoutAction(createClearTool(
					variableResolver, launcher), getStream(), getLoadRules(),
					isUseUpdate());
		}
		return action;
	}

	@Override
	protected PollAction createPollAction(VariableResolver variableResolver,
			ClearToolLauncher launcher,List<Filter> filters) {
		return new UcmPollAction(
				createClearTool(variableResolver, launcher),filters);
	}

	@Override
	protected ChangeLogAction createChangeLogAction(ClearToolLauncher launcher,
			AbstractBuild<?, ?> build, Launcher baseLauncher,List<Filter> filters) {
		VariableResolver variableResolver = new BuildVariableResolver(build,
				baseLauncher);

		UcmChangeLogAction action = new UcmChangeLogAction(createClearTool(
				variableResolver, launcher),filters);

        if (useDynamicView) {
			String extendedViewPath = viewDrive;
			if (!(viewDrive.endsWith("\\") && viewDrive.endsWith("/"))) {
				// Need to deteremine what kind of char to add in between
				if (viewDrive.contains("/")) {
					extendedViewPath += "/";
				} else {
					extendedViewPath += "\\";
				}
			}
			extendedViewPath += getViewName();
			action.setExtendedViewPath(extendedViewPath);
		}
		return action;
	}

	@Override
	protected SaveChangeLogAction createSaveChangeLogAction(
			ClearToolLauncher launcher) {
		return new UcmSaveChangeLogAction();
	}

	protected ClearTool createClearTool(VariableResolver variableResolver,
			ClearToolLauncher launcher) {
		if (useDynamicView) {
			return new ClearToolDynamicUCM(variableResolver, launcher,
					viewDrive);
		} else {
			return super.createClearTool(variableResolver, launcher);
		}
	}

	private String shortenStreamName(String longStream) {
		if (longStream.startsWith("stream:")) {
			return longStream.substring("stream:".length());
		}
		return longStream;
	}

	/**
	 * ClearCase UCM SCM descriptor
	 * 
	 * @author Erik Ramfelt
	 */
	public static final class ClearCaseUcmScmDescriptor extends
			SCMDescriptor<ClearCaseUcmSCM> implements ModelObject {

		protected ClearCaseUcmScmDescriptor() {
			super(ClearCaseUcmSCM.class, null);
			load();
		}

		@Override
		public String getDisplayName() {
			return "UCM ClearCase";
		}

		@Override
		public boolean configure(StaplerRequest req) {
			return true;
		}

		@Override
		public SCM newInstance(StaplerRequest req) throws FormException {
			ClearCaseUcmSCM scm = new ClearCaseUcmSCM(
					req.getParameter("ucm.stream"),
					req.getParameter("ucm.loadrules"),
					req.getParameter("ucm.viewname"),
					req.getParameter("ucm.usedynamicview") != null,
					req.getParameter("ucm.viewdrive"),
					req.getParameter("ucm.mkviewoptionalparam"),
					req.getParameter("ucm.filterOutDestroySubBranchEvent") != null,
					req.getParameter("ucm.useupdate") != null);
			return scm;
		}
	}
}
