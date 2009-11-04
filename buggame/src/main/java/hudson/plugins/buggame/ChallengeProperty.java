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
import hudson.plugins.buggame.goals.BuildGoal;
import hudson.plugins.buggame.goals.FindBugsGoal;
import hudson.plugins.buggame.goals.OpenTasksGoal;
import hudson.plugins.buggame.model.Goal;

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
		for (Challenge c: challenges) {
			c.setProject(this.owner);
		}
		
		return challenges;
	}
	
	public List<Challenge> getCurrentChallenges() {
		return getCurrentOrExpiredChallenges(true);
	}
	
	public List<Challenge> getExpiredChallenges() {
		return getCurrentOrExpiredChallenges(false);
	}
	
	public List<Challenge> getCurrentOrExpiredChallenges(boolean current) {
		List<Challenge> currentChallenges = new ArrayList<Challenge>();
		List<Challenge> expiredChallenges = new ArrayList<Challenge>();
		List<Challenge> challenges = getChallenges();
		DateTime now = new DateTime();
		
		for (Challenge c: challenges) {
			if (c.getEndDate().compareTo(now) < 0) {
				expiredChallenges.add(c);
			} else {
				currentChallenges.add(c);
			}
		}
		
		System.err.println("Current challenges: " + currentChallenges);
		System.err.println("Expired challenges: " + expiredChallenges);
		
		if (current) {
			return currentChallenges;
		} else {
			return expiredChallenges;
		}
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
			
			System.err.println("Req: " + req + "\n JSON: " + formData);
			
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
		private final String reward;
		private AbstractProject<?,?> project = null;
		private final Goal goal;
		
		@DataBoundConstructor
		public Challenge(String name, String startDate, String endDate,
				String reward, String goalStartValue, String goalEndValue, 
				String goalType) {
			this(name, bigEndianDateParser.parseDateTime(startDate), 
					bigEndianDateParser.parseDateTime(endDate),
					reward, Double.parseDouble(goalStartValue), Double.parseDouble(goalEndValue), goalType);
		}
		
		public Challenge (String name, DateTime startDate, 
				DateTime endDate, String reward, double goalStartValue, double goalEndValue, String goalType) {		
			this.name = Preconditions.checkNotNull(name);
			this.reward = Preconditions.checkNotNull(reward);
			this.startDate = Preconditions.checkNotNull(startDate);
			this.endDate = Preconditions.checkNotNull(endDate);
			Preconditions.checkArgument((this.startDate.compareTo(this.endDate) <= 0),
					"%s is after %s", this.startDate, this.endDate);
			
			if (goalType.equals("buildGoal")) {
				this.goal = new BuildGoal(this, goalEndValue);
			}
			else if (goalType.equals("findBugsGoal")) {
				this.goal = new FindBugsGoal(this, goalStartValue, goalEndValue);
			}
			else if (goalType.equals("tasksGoal")) {
				this.goal = new OpenTasksGoal(this, goalStartValue, goalEndValue);
			}
			else {
				throw new IllegalArgumentException("Goal unrecognized");
			}
		}

		public String getReward() {
			return reward;
		}

		public String getName() {
			return name;
		}
		
		public String getStartDateString() {
			return bigEndianDateParser.print(startDate);
		}

		public String getEndDateString() {
			return bigEndianDateParser.print(endDate);
		}
		
		public DateTime getStartDate() {
			return startDate;
		}
		
		public DateTime getEndDate() {
			return endDate;
		}
		
		public int getDaysLeft() {
			DateTime today = new DateTime();
			DateTime endDay = new DateTime(endDate);
			
			return Days.daysBetween(today, endDay).getDays();
		}
		
		public AbstractProject<?, ?> getProject() {
			return project;
		}

		public void setProject(AbstractProject<?, ?> project) {
			this.project = project;
		}

		public Goal getGoal() {
			return goal;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).
				append("name", name).
				append("startDate", startDate).
				append("endDate", endDate).
				append("reward", reward).
				toString();
		}
	}
}

