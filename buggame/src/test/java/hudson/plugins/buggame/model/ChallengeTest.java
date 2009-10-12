package hudson.plugins.buggame.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.FreeStyleProject;
import hudson.plugins.buggame.ChallengeProperty;
import hudson.plugins.buggame.ChallengeProperty.Challenge;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@SuppressWarnings("restriction")
public class ChallengeTest extends HudsonTestCase {
	Goal mockedGoal;
	AbstractProject<?, ?> mockedProject;
	DateTime today;
	static int i = 1;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mockedProject = createFreeStyleProject();
		today = new DateTime();
	}
	
	@Test
	public void testSimpleDaysConversion() throws InterruptedException {
		DateTime threeDays = today.plusDays(3); 
		
		Challenge challenge = new Challenge("Test challenge", today,
				threeDays, "Test reward", 1, 2, "buildGoal");
		
		// Sleep 5 seconds to ensure a separation when we calculate the days left
		Thread.sleep(5000);
		
		// After 5 seconds, there are now *2 days*, 23 hours and 59 minutes left
		assertEquals(2, Days.daysBetween(new DateTime(), threeDays).getDays());
		assertEquals(2, challenge.getDaysLeft());
	}
	
	@Test
	public void testSameDay() {
		Challenge challenge = new Challenge("Test challenge", today,
				today, "Test reward", 1, 2, "buildGoal");
		assertEquals(0, challenge.getDaysLeft());
	}

	
	@Test
	public void testToStringNotNull() {
		Challenge challenge = new Challenge("Test challenge", today,
				today, "Test reward", 1, 2, "buildGoal");
		assertFalse(challenge.toString() == "" || challenge.toString() == null);
	}
}
