package hudson.plugins.buggame;

import com.google.common.base.Preconditions;

import hudson.model.AbstractProject;
import hudson.model.Action;

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
	
	public String getChallenges() {
		ChallengeProperty cp = this.project.getProperty(ChallengeProperty.class);

		return cp.getChallenges().toString();
	}
}
