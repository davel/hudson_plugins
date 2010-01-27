package org.jvnet.hudson.plugins.jira.issueversioning.plugin.hudson.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for Jira Issue and Project Keys
 * 
 * @author <a href="mailto:from.hudson@nisgits.net">Stig Kleppe-J;odash&rgensen</a>
 */
public final class JiraKeyUtils {
	
	public static final Pattern DEFAULT_JIRA_PROJECT_KEY_PATTERN = Pattern.compile("\\b[A-Z]([A-Z]+)\\b");
	
	public static final Pattern DEFAULT_JIRA_ISSUE_KEY_PATTERN = Pattern.compile("\\b[a-zA-Z]([a-zA-Z]+)-([0-9]+)\\b");

	/**
	 * Validates if the given key {@link String} is a valid JIRA Project Key
	 * 
	 * @param key the project key to validate
	 * @return <code>true</code> if valid, <code>false</code> otherwise
	 * @see JiraKeyUtils#isValidProjectKey(String, String)
	 */
	public static boolean isValidProjectKey(String key) {
		return isValidProjectKey(key, DEFAULT_JIRA_PROJECT_KEY_PATTERN);
	}

	/**
	 * Validates if the given key {@link String} is a valid JIRA Project Key
	 * 
	 * @param key the project key to validate
	 * @param keyPattern the {@link Pattern} the key must adhere to
	 * @return <code>true</code> if valid, <code>false</code> otherwise
	 */
	public static boolean isValidProjectKey(String key, Pattern keyPattern) {
		Matcher matcher;
		if (keyPattern != null) {
			matcher = keyPattern.matcher(key);
		} else {
			matcher = DEFAULT_JIRA_PROJECT_KEY_PATTERN.matcher(key);
		}
		return matcher.find() && matcher.start() == 0 && matcher.end() == key.length();
	}

	/**
	 * Find all the Jira Project keys in the given text and return them in a {@link Set}
	 * 
	 * @param text the text to search for Jira Project keys
	 * @return the {@link Set} of found project keys
	 * @see JiraKeyUtils#getJiraProjectKeysFromText(String, String)
	 */
	public static Set<String> getJiraProjectKeysFromText(String text) {
		return getJiraProjectKeysFromText(text, DEFAULT_JIRA_PROJECT_KEY_PATTERN);
	}

	/**
	 * Find all the Jira Project keys in the given text and return them in a {@link Set}
	 * 
	 * @param text the text to search for Jira Project keys
	 * @param keyPattern the {@link Pattern} the key must adhere to
	 * @return the {@link Set} of found project keys
	 */
	public static Set<String> getJiraProjectKeysFromText(String text, Pattern keyPattern) {
		final Set<String> keys = new HashSet<String>();
		Matcher matcher;
		if (keyPattern != null) {
			matcher = keyPattern.matcher(text);
		} else {
			matcher = DEFAULT_JIRA_PROJECT_KEY_PATTERN.matcher(text);
		}
		while (matcher.find()) {
			keys.add(matcher.group());
		}
		return keys;
	}

	/**
	 * Validates if the given key {@link String} is a valid JIRA Issue Key
	 * 
	 * @param key the issue key to validate
	 * @return <code>true</code> if valid, <code>false</code> otherwise
	 * @see JiraKeyUtils#isValidIssueKey(String, String)
	 */
	public static boolean isValidIssueKey(String key) {
		return isValidIssueKey(key, DEFAULT_JIRA_ISSUE_KEY_PATTERN);
	}

	/**
	 * Validates if the given key {@link String} is a valid JIRA Issue Key
	 * 
	 * @param key the issue key to validate
	 * @param keyPattern the {@link Pattern} the key must adhere to
	 * @return <code>true</code> if valid, <code>false</code> otherwise
	 */
	public static boolean isValidIssueKey(String key, Pattern keyPattern) {
		Matcher matcher;
		if (keyPattern != null) {
			matcher = keyPattern.matcher(key);
		} else {
			matcher = DEFAULT_JIRA_ISSUE_KEY_PATTERN.matcher(key);
		}
		return matcher.find() && matcher.start() == 0 && matcher.end() == key.length();
	}

	/**
	 * Find all the Jira Issue keys in the given text and return them in a {@link Set}
	 * 
	 * @param text the text to search for Jira Issue keys
	 * @return the {@link Set} of found issue keys
	 * @see JiraKeyUtils#getJiraIssueKeysFromText(String, String)
	 */
	public static Set<String> getJiraIssueKeysFromText(String text) {
		return getJiraIssueKeysFromText(text, DEFAULT_JIRA_ISSUE_KEY_PATTERN);
	}

	/**
	 * Find all the Jira Issue keys in the given text and return them in a {@link Set}
	 * 
	 * @param text the text to search for Jira Issue keys
	 * @param keyPattern the {@link Pattern} the key must adhere to
	 * @return the {@link Set} of found issue keys
	 */
	public static Set<String> getJiraIssueKeysFromText(String text, Pattern keyPattern) {
		final Set<String> keys = new HashSet<String>();
		Matcher matcher;
		if (keyPattern != null) {
			matcher = keyPattern.matcher(text);
		} else {
			matcher = DEFAULT_JIRA_ISSUE_KEY_PATTERN.matcher(text);
		}
		while (matcher.find()) {
			keys.add(matcher.group());
		}
		return keys;
	}
	
}
