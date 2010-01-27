package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * TestCase for {@link JiraKeyUtils}
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
public class JiraKeyUtilsTest {

	/**
	 * Test validating a valid project key
	 */
	@Test
	public void testValidProjectKey() {
		assertTrue(JiraKeyUtils.isValidProjectKey("MARVADMIN"));
	}

	/**
	 * Test validating a valid project key
	 */
	@Test
	public void testValidProjectKeyOtherPattern() {
		assertTrue(JiraKeyUtils.isValidProjectKey("MArvADM1N", Pattern.compile("[A-Z]([a-zA-Z0-9]+)")));
	}

	/**
	 * Test invalidating a valid project key
	 */
	@Test
	public void testInvalidProjectKey() {
		assertFalse(JiraKeyUtils.isValidProjectKey("MARVADMIN2"));
		assertFalse(JiraKeyUtils.isValidProjectKey("MARVADMIN-2"));
		assertFalse(JiraKeyUtils.isValidProjectKey("marvADMIN"));
	}

	/**
	 * Test validating a valid project key
	 */
	@Test
	public void testInvalidProjectKeyOtherPattern() {
		assertFalse(JiraKeyUtils.isValidProjectKey("MArvADM1N-2", Pattern.compile("[A-Z]([a-zA-Z0-9]+)")));
	}

	/**
	 * Test invalidating a valid project key
	 */
	@Test
	public void testInvalidProjectKeyOtherPattern2() {
		assertFalse(JiraKeyUtils.isValidProjectKey("MARVADMIN2", null));
		assertFalse(JiraKeyUtils.isValidProjectKey("MARVADMIN-2", null));
		assertFalse(JiraKeyUtils.isValidProjectKey("marvADMIN", null));
	}

	/**
	 * Test get all Project keys for text
	 */
	@Test
	public void testGetProjectKeysFromText() {
		final String text = "This text contains two Project keys: MARVADMIN, MARVSITE";
		final Set<String> keys = JiraKeyUtils.getJiraProjectKeysFromText(text);
		assertTrue(keys.size() == 2);
		assertTrue(keys.contains("MARVADMIN"));
		assertTrue(keys.contains("MARVSITE"));
	}

	/**
	 * Test get all Project keys for text
	 */
	@Test
	public void testGetProjectKeysFromTextOtherPattern() {
		final String text = "This text contains two Project keys: MArvADM1N, MArvS1TE";
		final Set<String> keys =
			JiraKeyUtils.getJiraProjectKeysFromText(text, Pattern.compile("[A-Z]([a-zA-Z0-9]+)"));
		assertTrue(keys.size() == 4);
		assertTrue(keys.contains("MArvADM1N"));
		assertTrue(keys.contains("MArvS1TE"));
	}

	/**
	 * Test get all Project keys for text
	 */
	@Test
	public void testGetProjectKeysFromTextOtherPattern2() {
		final String text = "This text contains two Project keys: MARVADMIN, MARVSITE";
		final Set<String> keys =
			JiraKeyUtils.getJiraProjectKeysFromText(text, null);
		assertTrue(keys.size() == 2);
		assertTrue(keys.contains("MARVADMIN"));
		assertTrue(keys.contains("MARVSITE"));
	}

	/**
	 * Test validating a valid issue key
	 */
	@Test
	public void testValidIssueKey() {
		assertTrue(JiraKeyUtils.isValidIssueKey("MARVADMIN-2"));
		assertTrue(JiraKeyUtils.isValidIssueKey("MARVADMIN-20"));
		assertFalse(JiraKeyUtils.isValidIssueKey("MARVadmin2"));
	}

	/**
	 * Test validating a valid issue key
	 */
	@Test
	public void testValidIssueKeyOtherPattern() {
		assertTrue(JiraKeyUtils.isValidIssueKey("MARVadmin-2", Pattern.compile("[A-Z]([a-zA-Z0-9]+)-([0-9]+)")));
	}

	/**
	 * Test validating a valid issue key
	 */
	@Test
	public void testValidIssueKeyOtherPattern2() {
		assertTrue(JiraKeyUtils.isValidIssueKey("MARVadmin-2", null));
	}

	/**
	 * Test validating a invalid issue key
	 */
	@Test
	public void testInvalidIssueKey() {
		assertFalse(JiraKeyUtils.isValidIssueKey("MARVADMIN2"));
		assertFalse(JiraKeyUtils.isValidIssueKey("2-MARVADMIN"));
	}

	/**
	 * Test validating a valid issue key
	 */
	@Test
	public void testInvalidIssueKeyOtherPattern() {
		assertFalse(JiraKeyUtils.isValidIssueKey("MARVadmin2", Pattern.compile("[a-zA-Z]([a-zA-Z0-9]+)-([0-9]+)")));
	}

	/**
	 * Test validating a valid issue key
	 */
	@Test
	public void testInvalidIssueKeyOtherPattern2() {
		assertFalse(JiraKeyUtils.isValidIssueKey("MARVadmin2", null));
	}

	/**
	 * Test get all Issue keys for text
	 */
	@Test
	public void testGetIssueKeysFromText() {
		final String text = "This text contains two Issue keys: MARVADMIN-1, MARVSITE-10, marvadmin-2";
		final Set<String> keys = JiraKeyUtils.getJiraIssueKeysFromText(text);
		assertTrue(keys.size() == 3);
		assertTrue(keys.contains("MARVADMIN-1"));
		assertTrue(keys.contains("MARVSITE-10"));
		assertTrue(keys.contains("marvadmin-2"));
	}

	/**
	 * Test get all Issue keys for text
	 */
	@Test
	public void testGetIssueKeysFromTextOtherPattern() {
		final String text = "This text contains two Issue keys: MaRVaDM1N-1, MARVSITE-10, marvadmin-2";
		final Set<String> keys =
			JiraKeyUtils.getJiraIssueKeysFromText(text, Pattern.compile("[a-zA-Z]([a-zA-Z0-9]+)-([0-9]+)"));
		assertTrue(keys.size() == 3);
		assertTrue(keys.contains("MaRVaDM1N-1"));
		assertTrue(keys.contains("MARVSITE-10"));
		assertTrue(keys.contains("marvadmin-2"));
	}

	/**
	 * Test get all Issue keys for text
	 */
	@Test
	public void testGetIssueKeysFromTextOtherPattern2() {
		final String text = "This text contains two Issue keys: MARVADMIN-1, MARVSITE-10, marvadmin-2";
		final Set<String> keys = JiraKeyUtils.getJiraIssueKeysFromText(text, null);
		assertTrue(keys.size() == 3);
		assertTrue(keys.contains("MARVADMIN-1"));
		assertTrue(keys.contains("MARVSITE-10"));
		assertTrue(keys.contains("marvadmin-2"));
	}

}
