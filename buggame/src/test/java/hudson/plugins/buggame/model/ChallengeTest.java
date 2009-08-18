package hudson.plugins.buggame.model;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@SuppressWarnings("restriction")
public class ChallengeTest {
	Goal mockedGoal;
	DateTime today;
	
	@Before
	public void setUp() {
		mockedGoal = mock(Goal.class);
		today = new DateTime();
	}
	
	@Test
	public void simpleDaysConversion() {
		DateTime threeDays = today.plusDays(3); 
		
		Challenge challenge = new Challenge("Test challenge", today.toDate(),
				threeDays.toDate(), mockedGoal, "Test reward");
		
		// This gives you *two* days left, with it ending on the third day
		assertEquals(2, challenge.getDaysLeft());
	}
	
	@Test
	public void sameDay() {
		Challenge challenge = new Challenge("Test challenge", today.toDate(),
				today.toDate(), mockedGoal, "Test reward");
		assertEquals(0, challenge.getDaysLeft());
	}
	
	@Test
	public void toStringNotNull() {
		Challenge challenge = new Challenge("Test challenge", today.toDate(),
				today.toDate(), mockedGoal, "Test reward");
		assertFalse(challenge.toString() == "" || challenge.toString() == null);
	}
}
