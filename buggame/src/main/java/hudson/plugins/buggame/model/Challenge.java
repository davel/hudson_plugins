package hudson.plugins.buggame.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Preconditions;

public class Challenge {
	private final Date startDate;
	private final Date endDate;
	private final String name;
	private final Goal goal;
	private final String reward;
	
	public Challenge(String name, Date startDate, Date endDate, Goal goal, 
			String reward) {
		this.name = Preconditions.checkNotNull(name);
		this.reward = Preconditions.checkNotNull(reward);
		this.goal = Preconditions.checkNotNull(goal);
		this.startDate = new Date(Preconditions.checkNotNull(startDate.getTime()));
		this.endDate = new Date(Preconditions.checkNotNull(endDate.getTime()));
		
		Preconditions.checkArgument((this.startDate.compareTo(this.endDate) <= 0),
				"%s is after %s", this.startDate, this.endDate);
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
