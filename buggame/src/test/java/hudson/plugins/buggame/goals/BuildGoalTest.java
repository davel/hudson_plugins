package hudson.plugins.buggame.goals;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
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
			ScoreCard scoreCard = new ScoreCard();
			List<Score> scores = new LinkedList<Score>();
			scores.add(new Score("Build result", "Build result", 10, null));
			scoreCard.setScores(scores);
			
			ScoreCardAction scoreCardAction = new ScoreCardAction(scoreCard, build);
			build.addAction(scoreCardAction);
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
	 * Test method for {@link hudson.plugins.buggame.goals.BuildGoal#getStartValue()}.
	 */
	@Test
	public void testGetStartValue() {
		assertTrue(buildGoal.getStartValue() == 0);
	}
	
	private FreeStyleProject setUpHudsonProject() throws Exception {
		FreeStyleProject project = createFreeStyleProject();
		FreeStyleBuild[] builds = new FreeStyleBuild[3];
		DateTime buildDate = new DateTime(2009, 1, 1, 0, 0, 0, 0);
		
		// Create three builds, each with a date time mocked one day after the previous
		for (FreeStyleBuild build : builds) {
			build = spy(project.scheduleBuild2(0).get());
			when(build.getTimestamp()).thenReturn(buildDate.toGregorianCalendar());
			buildDate = buildDate.plusDays(1);
		}
		
		return project;
	}

}
