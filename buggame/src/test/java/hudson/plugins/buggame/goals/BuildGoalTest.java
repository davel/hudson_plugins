package hudson.plugins.buggame.goals;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.buggame.ScoreCardAction;
import hudson.plugins.buggame.model.Goal;
import hudson.plugins.buggame.model.Score;
import hudson.plugins.buggame.model.ScoreCard;
import static org.mockito.Mockito.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author cflewis
 *
 */
@SuppressWarnings("restriction")
public class BuildGoalTest extends HudsonTestCase {
	Goal buildGoal;
	double endValue = 10;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		buildGoal = new BuildGoal(mock(AbstractProject.class), endValue, new Date(), new Date());
	}

	/**
	 * Test method for {@link hudson.plugins.buggame.goals.BuildGoal#getEndValue()}.
	 */
	@Test
	public void testGetEndValue() {
		assertTrue(buildGoal.getEndValue() == endValue);
	}

	/**
	 * Test method for {@link hudson.plugins.buggame.goals.BuildGoal#getPercentageProgress()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetPercentageProgress() throws Exception {		
		FreeStyleProject project = setUpHudsonProject();
		AbstractBuild<?, ?> build = project.getLastBuild();
		double expectedScore = 0;
		
		while (build != null) {
			build.addAction(getScoreCardAction(build, "Build result", 10));

			expectedScore = expectedScore + 10;
			build = build.getPreviousBuild();
		}
		
		DateTime startDate = new DateTime(2008, 1, 1, 0, 0, 0, 0);
		DateTime endDate = new DateTime(2010, 1, 1, 0, 0, 0, 0);
		
		BuildGoal goal = new BuildGoal(project, 100, startDate.toDate(),
				endDate.toDate());
		
		assertTrue("Current score was " + goal.getCurrentScore() + "," +
				"but expected " + expectedScore, goal.getCurrentScore() == expectedScore);
	}
	
	/**
	 * Test method for {@link hudson.plugins.buggame.goals.BuildGoal#getPercentageProgress()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetSlicedPercentageProgress() throws Exception {		
		FreeStyleProject project = setUpHudsonProject();
		AbstractBuild<?, ?> build = project.getLastBuild();
		double expectedScore = 0;
		DateTime startDate = null;
		int i = 0;
		
		while (build != null) {
			build.addAction(getScoreCardAction(build, "Build result", 10));
			
			if (startDate == null) {expectedScore = expectedScore + 10;}			
			if (i == 4) {startDate = new DateTime(build.getTimestamp());}
			
			build = build.getPreviousBuild();
			i++;
		}
		
		DateTime endDate = new DateTime(2010, 1, 1, 0, 0, 0, 0);
		
		BuildGoal goal = new BuildGoal(project, 100, startDate.toDate(),
				endDate.toDate());
		
		System.err.println("Expected score was " + expectedScore);
		
		assertTrue("Current score was " + goal.getCurrentScore() + "," +
				"but expected " + expectedScore, goal.getCurrentScore() == expectedScore);
	}

	/**
	 * Test method for {@link hudson.plugins.buggame.goals.BuildGoal#getStartValue()}.
	 */
	@Test
	public void testGetStartValue() {
		assertTrue(buildGoal.getStartValue() == 0);
	}
	
	private FreeStyleProject setUpHudsonProject() throws Exception {
		FreeStyleProject project = createFreeStyleProject();
		FreeStyleBuild[] builds = new FreeStyleBuild[10];
		DateTime buildDate = new DateTime(2009, 1, 1, 0, 0, 0, 0);
		
		// Create three builds, each with a date time mocked one day after the previous
		for (FreeStyleBuild build : builds) {
			build = spy(project.scheduleBuild2(0).get());
			when(build.getTimestamp()).thenReturn(buildDate.toGregorianCalendar());
			buildDate = buildDate.plusDays(1);
		}
		
		return project;
	}
	
	private ScoreCardAction getScoreCardAction(AbstractBuild<?, ?> build, 
			String scoreName, int score) {
		ScoreCard scoreCard = new ScoreCard();
		List<Score> scores = new LinkedList<Score>();
		scores.add(new Score(scoreName, scoreName, score, null));
		scoreCard.setScores(scores);
		
		return new ScoreCardAction(scoreCard, build);
	}

}
