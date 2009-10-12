package hudson.plugins.buggame;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Preconditions;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.plugins.buggame.model.Goal;
import hudson.plugins.buggame.model.Challenge;

public final class ChallengeProperty extends
JobProperty<AbstractProject<?, ?>> {

	/**
	 * This is the list of challenges.
	 */
	private List<Challenge> challenges = new ArrayList<Challenge>();

	@DataBoundConstructor
	public ChallengeProperty(List<Challenge> challenges) {
		this.challenges = challenges;
	}
	
	public List<Challenge> getChallenges() {
		return this.challenges;
	}


	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {

		public DescriptorImpl() {
			super(ChallengeProperty.class);
			load();
		}
		
		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName() {
			return "Bug Game Challenges";
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {
			ChallengeProperty tpp = req.bindJSON(
					ChallengeProperty.class, formData);
			tpp.challenges = Descriptor.newInstancesFromHeteroList(req, formData, "challenges", Challenge.all());
			if (tpp.getChallenges() == null || tpp.getChallenges().isEmpty()) {
				tpp = null; // Not configured
			}
			
			return tpp;
		}
	}
}

