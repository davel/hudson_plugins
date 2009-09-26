package hudson.plugins.buggame;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Preconditions;

import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.plugins.buggame.model.Goal;

public class ChallengeProperty extends JobProperty<Job<?,?>> {
		private final int id;
		private final AbstractProject<?, ?> project;
		private final Date startDate;
		private final Date endDate;
		private final String name;
		private Goal goal = null;
		private final String reward;
		
		public ChallengeProperty (int id, AbstractProject<?, ?> project, String name, Date startDate, 
				Date endDate, String reward) {
			this.project = Preconditions.checkNotNull(project);
			//Preconditions.checkArgument(ChallengeProperty.getChallenge(id, this.project) == null);
			this.id = id;
			
			this.name = Preconditions.checkNotNull(name);
			this.reward = Preconditions.checkNotNull(reward);
			
			this.startDate = new Date(Preconditions.checkNotNull(startDate.getTime()));
			this.endDate = new Date(Preconditions.checkNotNull(endDate.getTime()));
			Preconditions.checkArgument((this.startDate.compareTo(this.endDate) <= 0),
					"%s is after %s", this.startDate, this.endDate);
		}
		
		public void setGoal(Goal goal) {
			Preconditions.checkState(this.goal == null);
			this.goal = goal;	
		}

		public Goal getGoal() {
			return goal;
		}

		public String getReward() {
			return reward;
		}

		public String getName() {
			return name;
		}
		
		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}
		
		public int getDaysLeft() {
			DateTime today = new DateTime();
			DateTime endDay = new DateTime(endDate);
			
			return Days.daysBetween(today, endDay).getDays();
		}
		
		@Override
		public String toString() {
			return new ToStringBuilder(this).
				append("ID", id).
				append("name", name).
				append("startDate", startDate).
				append("endDate", endDate).
				toString();
		}
		
		public int getId() {
			return this.id;
		}

		public AbstractProject<?, ?> getProject() {
			return project;
		}
	}

