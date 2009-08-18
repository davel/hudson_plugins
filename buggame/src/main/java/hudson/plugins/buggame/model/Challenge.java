package hudson.plugins.buggame.model;

import hudson.model.AbstractProject;
import hudson.model.Action;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Preconditions;

public class Challenge implements Action {
	private final AbstractProject<?, ?> project;
	private final Date startDate;
	private final Date endDate;
	private final String name;
	private Goal goal = null;
	private final String reward;
	
	public Challenge (AbstractProject<?, ?> project, String name, Date startDate, 
			Date endDate, String reward) {
		this.project = Preconditions.checkNotNull(project);
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
			append("name", name).
			append("startDate", startDate).
			append("endDate", endDate).
			toString();
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return null;
	}

	public AbstractProject<?, ?> getProject() {
		return project;
	}
}
