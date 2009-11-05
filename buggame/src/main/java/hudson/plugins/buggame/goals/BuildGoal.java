package hudson.plugins.buggame.goals;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterables;

import hudson.model.AbstractBuild;
import hudson.plugins.buggame.ScoreCardAction;
import hudson.plugins.buggame.ChallengeProperty.Challenge;
import hudson.plugins.buggame.model.Goal;
import hudson.plugins.buggame.model.Score;
import hudson.plugins.buggame.model.ScoreCard;

public class BuildGoal extends Goal {
	private final static String goalName = "Build result";
	
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
		return getGeneralScore(goalName, false);
	}

	@Override
	public boolean isClass(String className) {
    	return (className.equals("buildGoal")) ? true : false;
    }
}
