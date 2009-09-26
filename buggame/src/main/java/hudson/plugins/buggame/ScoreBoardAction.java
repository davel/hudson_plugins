package hudson.plugins.buggame;

import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.plugins.buggame.model.Challenge;

/**
 * Score card for a job.
 * 
 * @author Erik Ramfelt
 */
public class ScoreBoardAction implements Action {

    private static final long serialVersionUID = 1L;
    private final AbstractProject<?, ?> project;
    
    public ScoreBoardAction(AbstractProject<?, ?> project) {
    	this.project = project;
    	
    	System.err.println("Loaded Scoreboard action");
    }
    
    /**
     * Return the score for a team from scorecards.
     * @return
     */
    public double getProjectScore() {
    	double score = 0;
    	
    	AbstractBuild<?, ?> build = project.getLastCompletedBuild();
    	
    	while (build != null) {
    		List <ScoreCardAction> actions = build.getActions(ScoreCardAction.class);

    		for (ScoreCardAction action : actions) {
    			score = score + action.getScorecard().getTotalPoints();
    		}
    		
    		build = build.getPreviousBuild();
    	}
    	
    	return score;
    }
    
    public String getDisplayName() {
        return "Challenge Score Board";
    }

    public String getIconFileName() {
        return GameDescriptor.ACTION_LOGO_MEDIUM;
    }

    public String getUrlName() {
        return "buggame";
    }

	public AbstractProject<?, ?> getProject() {
		return project;
	}
	
	public Challenge getChallenge(String id) {
		if (id == null) { id = "1"; }
		return getChallenge(Integer.parseInt(id));
	}
	
	public Challenge getChallenge(int id) {
		// FIXME: This is returning a fake object
		AbstractProject<?, ?> mockProject = mock(AbstractProject.class);
		Challenge mockChallenge = new Challenge(1, mockProject, "Test challenge",
				new Date(), new Date(), "Test Reward");
		
		return mockChallenge;
	}

}
