package hudson.plugins.buggame;

import java.util.List;

import com.google.common.base.Preconditions;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.plugins.buggame.ChallengeProperty.Challenge;

public class ChallengeProjectAction implements Action {
	AbstractProject<?,?> project;
	
	public ChallengeProjectAction(AbstractProject<?,?> project) {
		this.project = Preconditions.checkNotNull(project);
	}
	
	public AbstractProject<?,?> getProject() {
		return project;
	}
	
	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Hello";
	}

	@Override
	public String getIconFileName() {
		// TODO Auto-generated method stub
        return GameDescriptor.ACTION_LOGO_MEDIUM;
	}

	@Override
	public String getUrlName() {
		// TODO Auto-generated method stub
		return "buggame";
	}
	
	public List<Challenge> getChallenges() {
		ChallengeProperty cp = this.project.getProperty(ChallengeProperty.class);

		return cp.getChallenges();
	}
	
	public List<Challenge> getCurrentChallenges() {
		ChallengeProperty cp = this.project.getProperty(ChallengeProperty.class);

		return cp.getCurrentChallenges();
	}
	
	public List<Challenge> getExpiredChallenges() {
		ChallengeProperty cp = this.project.getProperty(ChallengeProperty.class);

		return cp.getExpiredChallenges();
	}
}
