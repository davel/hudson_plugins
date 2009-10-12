package hudson.plugins.buggame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
//import hudson.plugins.buggame.model.Goal;

public final class ChallengeProperty extends
JobProperty<AbstractProject<?, ?>> {
	public static final DateTimeFormatter bigEndianDateParser = DateTimeFormat.forPattern("yyyy-MM-dd");


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
			if (req == null || formData == null) { return null; }
			
			ChallengeProperty tpp = req.bindJSON(
					ChallengeProperty.class, formData);
			if (tpp == null || tpp.getChallenges() == null || tpp.getChallenges().isEmpty()) {
				tpp = null; // Not configured
			}
			
			return tpp;
		}
	}
	
	public static final class Challenge implements Serializable {
		private static final long serialVersionUID = -4216410509576915952L;
		private final DateTime startDate;
		private final DateTime endDate;
		private final String name;
		//private Goal goal = null;
		private final String reward;
		
		@DataBoundConstructor
		public Challenge(String name, String startDate, String endDate,
				String reward) {
			this(name, bigEndianDateParser.parseDateTime(startDate), 
					bigEndianDateParser.parseDateTime(endDate),
					reward);
		}
		
		public Challenge (String name, Date startDate, Date endDate,
				String reward) {
			this(name, new DateTime(Preconditions.checkNotNull(startDate.getTime())), 
					new DateTime(Preconditions.checkNotNull(endDate.getTime())), reward);
		}
		
		public Challenge (String name, DateTime startDate, 
				DateTime endDate, String reward) {		
			this.name = Preconditions.checkNotNull(name);
			this.reward = Preconditions.checkNotNull(reward);
			this.startDate = Preconditions.checkNotNull(startDate);
			this.endDate = Preconditions.checkNotNull(endDate);
			Preconditions.checkArgument((this.startDate.compareTo(this.endDate) <= 0),
					"%s is after %s", this.startDate, this.endDate);
		}
		
//		public void setGoal(Goal goal) {
//			Preconditions.checkState(this.goal == null);
//			this.goal = goal;	
//		}
//
//		public Goal getGoal() {
//			return goal;
//		}

		public String getReward() {
			return reward;
		}

		public String getName() {
			return name;
		}
		
		public String getStartDate() {
			return bigEndianDateParser.print(startDate);
		}

		public String getEndDate() {
			return bigEndianDateParser.print(endDate);
		}
		
		public int getDaysLeft() {
			DateTime today = new DateTime();
			DateTime endDay = new DateTime(endDate);
			
			return Days.daysBetween(today, endDay).getDays();
		}
		
		@Override
		public String toString() {
			return new ToStringBuilder(this).
				append("name", name).
				append("startDate", startDate).
				append("endDate", endDate).
				toString();
		}
	}
}

