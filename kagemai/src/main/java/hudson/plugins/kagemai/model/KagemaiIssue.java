package hudson.plugins.kagemai.model;

import java.io.Serializable;

/**
 * Kagemai Issue.
 * 
 * @author yamkazu
 * 
 */
@SuppressWarnings("serial")
public class KagemaiIssue implements Serializable, Comparable<KagemaiIssue> {

	private int id;
	private String summary;

	public KagemaiIssue(int id, String summary) {
		this.id = id;
		this.summary = summary;
	}

	public int getId() {
		return id;
	}

	public String getSummary() {
		return summary;
	}

	public int compareTo(KagemaiIssue issue) {
		if (this.id < issue.getId()) {
			return -1;
		} else if (this.id > issue.getId()) {
			return 1;
		} else {
			return 0;
		}
	}

}
