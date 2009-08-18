package hudson.plugins.buggame.model;

import org.joda.time.DateTime;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@SuppressWarnings("restriction")
public class ChallengeTest {
	
	@Test
	public void simpleDaysConversion() {
		Goal mockedGoal = mock(Goal.class);
		DateTime today = new DateTime();
		DateTime threeDays = today.plusDays(3); 
		
		Challenge challenge = new Challenge("Test challenge", today.toDate(),
				threeDays.toDate(), mockedGoal, "Test reward");
		
		// This gives you *two* days left, with it ending on the third day
		assertEquals(2, challenge.getDaysLeft());
	}
	
	@Test
	public void sameDay() {
		Goal mockedGoal = mock(Goal.class);
		DateTime today = new DateTime();
		
		Challenge challenge = new Challenge("Test challenge", today.toDate(),
				today.toDate(), mockedGoal, "Test reward");
		assertEquals(0, challenge.getDaysLeft());
	}
}
