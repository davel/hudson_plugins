package de.fspengler.hudson.pview;

import org.kohsuke.stapler.export.Exported;



public class DirEntry {

	private String directory;
	private String link;

	private int counter;

	public DirEntry(String directory, String link) {
		super();
		this.directory = directory;
		this.link = link;
		this.counter = 1;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void addOne() {
		++counter;
	}

	public String toString(){
		return directory + "##" + counter;
	}
}
