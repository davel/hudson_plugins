package hudson.plugins.buggame.goals;

import java.util.Collection;
import java.util.Date;

import org.joda.time.DateTime;

import com.google.common.collect.Iterables;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.buggame.ScoreCardAction;
import hudson.plugins.buggame.model.Goal;
import hudson.plugins.buggame.model.Score;
import hudson.plugins.buggame.model.ScoreCard;

public class BuildGoal extends Goal {
	AbstractProject<?, ?> project;
	double endValue;
	DateTime startDate;
	DateTime endDate;
	String ruleName = "Build result";
	
	public BuildGoal(AbstractProject<?, ?> project, double endValue, 
			Date startDate, Date endDate) {
		// Start value has no context in this goal, so it is set to 0
		super(project, 0, endValue, startDate, endDate);
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
	
	public double getCurrentScore() {
		AbstractBuild<?, ?> startBuild = getStartBuild();
		AbstractBuild<?, ?> endBuild = getEndBuild();
		AbstractBuild<?, ?> build = startBuild;
		double totalScore = getStartValue(); 
				
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
				
		return totalScore;
	}
}
