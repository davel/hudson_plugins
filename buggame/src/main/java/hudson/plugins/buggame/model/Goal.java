package hudson.plugins.buggame.model;

/**
 * An interface to the goal of a challenge.
 * 
 * @author Chris Lewis
 *
 */
public interface Goal {
	/**
	 * Returns the name of the goal
	 * 
	 * @return name of the goal
	 */
	public String getName();
	
	/**
	 * Returns the percentage progress towards the goal.
	 * 
	 * @return percentage progress towards the goal
	 */
	public double getPercentageProgress();
	
	/**
	 * Returns the start value for this goal.
	 * 
	 * @return start value of goal
	 */
	public double getStartValue();
	
	/**
	 * Returns the end value for this goal
	 * 
	 * @return end value of goal
	 */
	public double getEndValue();
}
