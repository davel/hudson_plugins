package hudson.plugins.buggame;

import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

/**
 * Score card for a job.
 * 
 * @author Erik Ramfelt
 */
public class ScoreBoardAction implements Action {

    private static final long serialVersionUID = 1L;
    private final AbstractProject<?,?> project;
    
    public ScoreBoardAction(AbstractProject<?,?> project) {
    	this.project = project;
    }
    
    /**
     * Return the score for a team from scorecards.
     * TODO: Implement this
     * @return
     */
    public double getProjectScore() {
    	double score = 0;
    	
    	AbstractBuild<?,?> build = project.getLastCompletedBuild();
    	
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
        return "Score card";
    }

    public String getIconFileName() {
        return "Scorecard.gif";
    }

    public String getUrlName() {
        return "buggame";
    }

	public AbstractProject<?,?> getProject() {
		return project;
	}

}
