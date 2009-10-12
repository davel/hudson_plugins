package hudson.plugins.buggame.model;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.buggame.ChallengeProperty;
import hudson.plugins.buggame.ChallengeProperty.Challenge;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

/**
 * An interface to the goal of a challenge.
 * 
 * @author Chris Lewis
 *
 */
public abstract class Goal {
	double startValue;
	double endValue;
	DateTime startDate;
	DateTime endDate;
	Challenge challenge;
	
	public Goal(Challenge challenge, double startValue, double endValue) {
		this.challenge = Preconditions.checkNotNull(challenge);
		this.startValue = startValue;
		this.endValue = endValue;
		this.startDate = new DateTime(challenge.getStartDate());
		this.endDate = new DateTime(challenge.getEndDate());
	}

	
	/**
	 * Returns the name of the goal
	 * 
	 * @return name of the goal
	 */
	public abstract String getName();
	
	public abstract double getCurrentScore();
	
	/**
	 * Returns the percentage progress towards the goal.
	 * 
	 * @return percentage progress towards the goal
	 */
	public double getPercentageProgress() {
		return (getCurrentScore() / getEndValue()) * 100;
	}
	
	/**
	 * Returns the start value for this goal.
	 * 
	 * @return start value of goal
	 */
	public double getStartValue() {
		return startValue;
	}
	
	/**
	 * Returns the end value for this goal
	 * 
	 * @return end value of goal
	 */
	public double getEndValue() {
		return endValue;
	}
	
	protected AbstractBuild<?, ?> getStartBuild() {
		return getDatedBuild(startDate, true);
	}
	
	protected AbstractBuild<?, ?> getEndBuild() {
		return getDatedBuild(endDate, false);
	}

	protected AbstractBuild<?, ?> getDatedBuild(DateTime compareDate, boolean getNewerThanBoundary) {
		AbstractBuild<?, ?> build = challenge.getProject().getLastCompletedBuild();
		AbstractBuild<?, ?> lastBuild = null;
		
		while (build != null) {
			assert (build.getTimestamp().compareTo(build.getPreviousBuild().getTimestamp()) >= 0);
    		DateTime buildTime = new DateTime(build.getTimestamp());
    		if (buildTime.compareTo(compareDate) == -1) { break; }
    		lastBuild = build;
    		build = build.getPreviousBuild();
    	}
    	
		// We broke when we went over the boundary, so roll back one
		if (getNewerThanBoundary) { build = lastBuild; }
		
    	return build;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("name", getName()).
			append("startDate", startDate).
			append("endDate", endDate).
			append("endValue", endValue).
			toString();
	}
}
