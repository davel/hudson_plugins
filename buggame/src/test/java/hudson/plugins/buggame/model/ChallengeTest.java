//package hudson.plugins.buggame.model;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import hudson.model.AbstractProject;
//import hudson.model.Action;
//import hudson.model.Actionable;
//import hudson.model.FreeStyleProject;
//import hudson.plugins.buggame.ChallengeProperty;
//
//import org.joda.time.DateTime;
//import org.joda.time.Days;
//import org.junit.Before;
//import org.junit.Test;
//import org.jvnet.hudson.test.HudsonTestCase;
//
//import static org.mockito.Mockito.*;
//import static org.junit.Assert.*;
//
//
//@SuppressWarnings("restriction")
//public class ChallengeTest extends HudsonTestCase {
//	Goal mockedGoal;
//	AbstractProject<?, ?> mockedProject;
//	DateTime today;
//	static int i = 1;
//	
//	@SuppressWarnings("unchecked")
//	@Before
//	public void setUp() throws Exception {
//		super.setUp();
//		mockedGoal = mock(Goal.class);
//		mockedProject = createFreeStyleProject();
//		today = new DateTime();
//	}
//	
//	@Test
//	public void testSimpleDaysConversion() throws InterruptedException {
//		DateTime threeDays = today.plusDays(3); 
//		
//		Challenge challenge = new Challenge(1, mockedProject, "Test challenge", today.toDate(),
//				threeDays.toDate(), "Test reward");
//		challenge.setGoal(mockedGoal);
//		
//		// Sleep 5 seconds to ensure a separation when we calculate the days left
//		Thread.sleep(5000);
//		
//		// After 5 seconds, there are now *2 days*, 23 hours and 59 minutes left
//		assertEquals(2, Days.daysBetween(new DateTime(), threeDays).getDays());
//		assertEquals(2, challenge.getDaysLeft());
//	}
//	
//	@Test
//	public void testSameDay() {
//		Challenge challenge = new Challenge(2, mockedProject, "Test challenge", today.toDate(),
//				today.toDate(), "Test reward");
//		challenge.setGoal(mockedGoal);
//		assertEquals(0, challenge.getDaysLeft());
//	}
//	
//	@Test
//	public void testFailSetGoalTwice() {
//		Challenge challenge = new Challenge(3, mockedProject, "Test challenge", today.toDate(),
//				today.toDate(), "Test reward");
//		challenge.setGoal(mockedGoal);
//		
//		try {
//			challenge.setGoal(mockedGoal);
//			// If this doesn't throw an exception, something was wrong
//			fail("No state exception thrown when setting goal twice");
//		} catch (IllegalStateException e) {
//			// That works
//		}
//	}
//	
//	@Test
//	public void testToStringNotNull() {
//		Challenge challenge = new Challenge(4, mockedProject, "Test challenge", today.toDate(),
//				today.toDate(), "Test reward");
//		challenge.setGoal(mockedGoal);
//		assertFalse(challenge.toString() == "" || challenge.toString() == null);
//	}
//	
//	// This doesn't work as the action list is immutable
////	@Test(expected=IllegalArgumentException.class)
////	public void testClashingChallengeIDs() {
////		Challenge challenge = new Challenge(5, mockedProject, "Test challenge", today.toDate(),
////				today.toDate(), "Test reward");
////		mockedProject.addAction(challenge);
////
////		Challenge challenge2 = new Challenge(5, mockedProject, "Test challenge", today.toDate(),
////				today.toDate(), "Test reward");
////		mockedProject.addAction(challenge2);
////	}
//	
//	public void testChallengeProperties() throws IOException {
//		ChallengeProperty challenge = new ChallengeProperty(5, mockedProject, "Test challenge", today.toDate(),
//				today.toDate(), "Test reward");
//		mockedProject.addProperty(challenge);
//		ChallengeProperty challenge2 = new ChallengeProperty(6, mockedProject, "Test challenge", today.toDate(),
//				today.toDate(), "Test reward");
//		mockedProject.addProperty(challenge2);
//	}
//}
