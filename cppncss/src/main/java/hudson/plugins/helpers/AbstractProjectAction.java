package hudson.plugins.helpers;

import java.io.File;

import hudson.model.AbstractProject;
import hudson.model.Actionable;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * An action that is associated with a project.
 * 
 * @author Stephen Connolly
 * @param <PROJECT>
 *            the type of project that this action is associated with.
 * @since 04-Feb-2008 19:42:40
 */
abstract public class AbstractProjectAction<PROJECT extends AbstractProject<?, ?>>
		extends Actionable {
	/**
	 * The owner of this action.
	 */
	private final PROJECT project;
	private final Integer functionCcnViolationThreshold;
	private final Integer functionNcssViolationThreshold;

	protected AbstractProjectAction(PROJECT project,
			Integer functionCcnViolationThreshold,
			Integer functionNcssViolationThreshold) {
		this.project = project;
		this.functionCcnViolationThreshold = functionCcnViolationThreshold;
		this.functionNcssViolationThreshold = functionNcssViolationThreshold;
	}

	/**
	 * Getter for property 'project'.
	 * 
	 * @return Value for property 'project'.
	 */
	public PROJECT getProject() {
		return project;
	}

	/**
	 * Override to control when the floating box should be displayed.
	 * 
	 * @return <code>true</code> if the floating box should be visible.
	 */
	public boolean isFloatingBoxActive() {
		return true;
	}

	/**
	 * Override to control when the action displays a trend graph.
	 * 
	 * @return <code>true</code> if the action should show a trend graph.
	 */
	public boolean isGraphActive() {
		return false;
	}

	/**
	 * Override to define the graph name.
	 * 
	 * @return The graph name.
	 */
	public String getGraphName() {
		return getDisplayName();
	}

	public Integer getFunctionCcnViolationThreshold() {
		return functionCcnViolationThreshold;
	}

	public Integer getFunctionNcssViolationThreshold() {
		return functionNcssViolationThreshold;
	}

}
