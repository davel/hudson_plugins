package hudson.plugins.buggame.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class Challenge {
	private final Date startDate;
	private final Date endDate;
	private final String name;
	private final Goal goal;
	private final String reward;
	
	public Challenge(String name, Date startDate, Date endDate, Goal goal, 
			String reward) {
		if (name == null || startDate == null || endDate == null || 
				goal == null || reward == null) {
			throw new IllegalArgumentException("Null values not allowed");
		}
		
		this.name = name;
		this.reward = reward;
		this.goal = goal;
		this.startDate = new Date(startDate.getTime());
		this.endDate = new Date(endDate.getTime());
		
		if (this.startDate.compareTo(this.endDate) > 0) {
			throw new IllegalArgumentException(this.startDate + " is after " + 
					this.endDate);
		}
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
