package hudson.plugins.buggame.goals;

import java.util.Collection;

import com.google.common.collect.Iterables;

import hudson.model.AbstractBuild;
import hudson.plugins.buggame.ScoreCardAction;
import hudson.plugins.buggame.model.Challenge;
import hudson.plugins.buggame.model.Goal;
import hudson.plugins.buggame.model.Score;
import hudson.plugins.buggame.model.ScoreCard;

public class BuildGoal extends Goal {
	private final static String ruleName = "Build result";
	
	public BuildGoal(Challenge challenge, double endValue) {
		// Start value has no context in this goal, so it is set to 0
		super(challenge, 0, endValue);
	}

	@Override
	public String getName() {
		return "Build goal";
	}
	
	@Override
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
