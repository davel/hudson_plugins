package hudson.plugins.git;

public class GitException extends RuntimeException {
	public GitException() {
		super();
	}

	public GitException(String message) {
		super(message);
	}

	public GitException(String message, Throwable cause) {
		super(message, cause);
	}
}
