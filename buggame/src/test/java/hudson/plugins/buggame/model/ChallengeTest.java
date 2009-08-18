package hudson.plugins.buggame.model;

import hudson.model.AbstractProject;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@SuppressWarnings("restriction")
public class ChallengeTest {
	Goal mockedGoal;
	AbstractProject<?, ?> mockedProject;
	DateTime today;
	
	@Before
	public void setUp() {
		mockedGoal = mock(Goal.class);
		mockedProject = mock(AbstractProject.class);
		today = new DateTime();
	}
	
	@Test
	public void simpleDaysConversion() throws InterruptedException {
		DateTime threeDays = today.plusDays(3); 
		
		Challenge challenge = new Challenge(mockedProject, "Test challenge", today.toDate(),
				threeDays.toDate(), "Test reward");
		challenge.setGoal(mockedGoal);
		
		// Sleep 5 seconds to ensure a separation when we calculate the days left
		Thread.sleep(5000);
		
		// After 5 seconds, there are now *2 days*, 23 hours and 59 minutes left
		assertEquals(2, Days.daysBetween(new DateTime(), threeDays).getDays());
		assertEquals(2, challenge.getDaysLeft());
	}
	
	@Test
	public void sameDay() {
		Challenge challenge = new Challenge(mockedProject, "Test challenge", today.toDate(),
				today.toDate(), "Test reward");
		challenge.setGoal(mockedGoal);
		assertEquals(0, challenge.getDaysLeft());
	}
	
	@Test
	public void failSetGoalTwice() {
		Challenge challenge = new Challenge(mockedProject, "Test challenge", today.toDate(),
				today.toDate(), "Test reward");
		challenge.setGoal(mockedGoal);
		
		try {
			challenge.setGoal(mockedGoal);
			// If this doesn't throw an exception, something was wrong
			fail("No state exception thrown when setting goal twice");
		} catch (IllegalStateException e) {
			// That works
		}
	}
	
	@Test
	public void toStringNotNull() {
		Challenge challenge = new Challenge(mockedProject, "Test challenge", today.toDate(),
				today.toDate(), "Test reward");
		challenge.setGoal(mockedGoal);
		assertFalse(challenge.toString() == "" || challenge.toString() == null);
	}
}
