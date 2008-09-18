package hudson.plugins.crap4j.util;

import hudson.FilePath;

import java.io.Serializable;
import java.nio.charset.Charset;

public class FoundFile implements Serializable {

	private static final long serialVersionUID = 3122522673476247421L;
	private final FilePath file;
	private final String relativePath;
	private final String encoding;

	public FoundFile(FilePath file, String relativePath) {
		super();
		this.file = file;
		this.relativePath = relativePath;
		this.encoding = Charset.defaultCharset().name();
	}

	public FilePath getFile() {
		return this.file;
	}

	public String getRelativePath() {
		return this.relativePath;
	}

	public String getEncoding() {
		return this.encoding;
	}
}
