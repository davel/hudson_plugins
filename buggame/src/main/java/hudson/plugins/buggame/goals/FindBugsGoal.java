package hudson.plugins.buggame.goals;

import hudson.plugins.buggame.ChallengeProperty.Challenge;
import hudson.plugins.buggame.model.Goal;

public class FindBugsGoal extends Goal {
	private final static String goalName = "(.*)Findbugs(.*)";

	public FindBugsGoal(Challenge challenge, double startValue, double endValue) {
		super(challenge, startValue, endValue);
	}

	@Override
	public double getCurrentScore() {
		// TODO Auto-generated method stub
		return getGeneralScore(goalName, false);
	}

	@Override
	public String getName() {
		return "FindBugs goal";
	}
	
	@Override
    public boolean isClass(String className) {
    	return (className.equals("findBugsGoal")) ? true : false;
    }
}
