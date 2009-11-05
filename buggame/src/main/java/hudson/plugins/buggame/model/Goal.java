package hudson.plugins.buggame.model;

import java.util.Collection;
import java.util.NoSuchElementException;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.buggame.ChallengeProperty;
import hudson.plugins.buggame.ScoreCardAction;
import hudson.plugins.buggame.ChallengeProperty.Challenge;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

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
		this.startDate = challenge.getStartDate();
		this.endDate = challenge.getEndDate();
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
	public long getPercentageProgress() {
		long roundedPercentage = Math.round(((getCurrentScore() - getStartValue()) / (getEndValue() - getStartValue())) * 100);
		return roundedPercentage;
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
		
		/**Add one onto the day, as when people put in time limits, they usually
		* mean it inclusively: if my goal ends on the 24th, that means all
		* submissions on the 24th should be included too.
		*/ 
		compareDate = compareDate.plusDays(1);
		
		while (build != null) {
			assert (build.getTimestamp().compareTo(build.getPreviousBuild().getTimestamp()) >= 0);
    		DateTime buildTime = new DateTime(build.getTimestamp());
    		if (buildTime.compareTo(compareDate) == -1) { break; }
    		lastBuild = build;
    		build = build.getPreviousBuild();
    	}
    	
		// We broke when we went over the boundary, so roll back one
		if (getNewerThanBoundary) { build = lastBuild; }
				
		System.err.println("Returning build dated " + build.getTimestampString());

    	return build;
	}
	
	protected double getGeneralScore(String goalName, boolean startScoreZero) {
		AbstractBuild<?, ?> startBuild = getStartBuild();
		AbstractBuild<?, ?> endBuild = getEndBuild();
		AbstractBuild<?, ?> build = startBuild;
		double totalScore = (startScoreZero) ? 0 : getStartValue();
				
		while (build != null && build.getActions(ScoreCardAction.class) != null) {
			ScoreCardAction scoreCardAction;
			
			try {
				scoreCardAction = Iterables.getOnlyElement(build.getActions(ScoreCardAction.class));
			} catch (NoSuchElementException e) {
				if (build.equals(endBuild)) { break; }
				
				build = build.getNextBuild();
				
				continue;
			}
			
			totalScore = totalScore + getBuildScore(build, goalName);
			
			// Only break after we've iterated one last time to get this build
			if (build.equals(endBuild)) { break; }
			
			build = build.getNextBuild();
		}
				
		return totalScore;
	}
	
	protected double getBuildScore(AbstractBuild<?,?> build, String goalName) {
		double returnScore = 0;
		
		ScoreCardAction scoreCardAction = Iterables.getOnlyElement(build.getActions(ScoreCardAction.class));
		ScoreCard scoreCard = scoreCardAction.getScorecard();
		
		Collection<Score> scores = scoreCard.getScores();
		
		for (Score score : scores) {
			System.err.println("Checking score for build dated " + build.getTimestampString());
			System.err.println("Score rule is " + score.getRuleName());
			if (score.getRuleName().matches(goalName)) {
				System.err.println("Adding " + score.getValue() + " to total");
				returnScore = returnScore + score.getValue();
			}
		}
		
		return returnScore;
	}
	
	public abstract boolean isClass(String className);

	
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
