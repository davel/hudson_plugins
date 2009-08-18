package hudson.plugins.buggame.goals;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.google.common.collect.Iterables;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.plugins.buggame.ScoreCardAction;
import hudson.plugins.buggame.model.Goal;
import hudson.plugins.buggame.model.Score;
import hudson.plugins.buggame.model.ScoreCard;

public class BuildGoal implements Goal {
	AbstractProject<?, ?> project;
	double endValue;
	DateTime startDate;
	DateTime endDate;
	String ruleName = "Build result";
	
	public BuildGoal(AbstractProject<?, ?> project, double endValue, 
			Date startDate, Date endDate) {
		this.project = project;
		this.endValue = endValue;
		this.startDate = new DateTime(startDate);
		this.endDate = new DateTime(endDate);
	}

	@Override
	public double getEndValue() {
		return endValue;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Build goal";
	}

	@Override
	public double getPercentageProgress() {
		return (getCurrentScore() / getEndValue()) * 100;
	}

	/**
	 * This always returns 0, it has no meaning in the context of this goal.
	 */
	@Override
	public double getStartValue() {
		return 0;
	}
	
	private AbstractBuild<?, ?> getStartBuild() {
		return getDatedBuild(startDate, true);
	}
	
	private AbstractBuild<?, ?> getEndBuild() {
		return getDatedBuild(endDate, false);
	}

	private AbstractBuild<?, ?> getDatedBuild(DateTime compareDate, boolean getNewerThanBoundary) {
		AbstractBuild<?, ?> build = project.getLastCompletedBuild();
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
	
	public double getCurrentScore() {
		AbstractBuild<?, ?> startBuild = getStartBuild();
		AbstractBuild<?, ?> endBuild = getEndBuild();
		AbstractBuild<?, ?> build = startBuild;
		double totalScore = getStartValue(); 
		
		System.err.println("Starting getCurrentScore loop");
		
		while (build != null) {
			ScoreCardAction scoreCardAction = Iterables.getOnlyElement(build.getActions(ScoreCardAction.class));
			ScoreCard scoreCard = scoreCardAction.getScorecard();
			
			Collection<Score> scores = scoreCard.getScores();
			
			for (Score score : scores) {
				if (score.getRuleName() == ruleName) {
					totalScore = totalScore + score.getValue();
				}
			}
			
			// Only break after we've iterated one last time to get this build
			if (build.equals(endBuild)) { break; }
			
			build = build.getNextBuild();
		}
		
		System.err.println("Ending getCurrentScore loop");
		
		return totalScore;
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
