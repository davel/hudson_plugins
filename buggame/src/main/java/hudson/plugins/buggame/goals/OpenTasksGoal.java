package hudson.plugins.buggame.goals;

import hudson.model.AbstractBuild;
import hudson.plugins.buggame.ChallengeProperty.Challenge;
import hudson.plugins.buggame.model.Goal;

public class OpenTasksGoal extends Goal {
	private final static String goalName = "(.*)tasks(.*)";

	public OpenTasksGoal(Challenge challenge, double startValue, double endValue) {
		super(challenge, startValue, endValue);
	}

	@Override
	public double getCurrentScore() {
		/** 
		 * The open tasks plugin will flag and score every
		 * open task on a build, but the score card only shows the changes.
		 * This is a different behavior to other modules, which score
		 * incrementally, only scoring the changed tasks.
		 */
		//
		//return getBuildScore(getEndBuild(), goalName);
		System.out.println("=========");
		double s = getGeneralScore(goalName, true);
		System.out.println("Score is " + s);
		System.out.println("=========");
		return s;
	}

	@Override
	public String getName() {
		return "Open Tasks goal";
	}
	
	
}
