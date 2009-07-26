package hudson.plugins.buckminster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hudson.model.BuildListener;

/**
 * A separate thread that reads the output stream of a given {@link Process} object and writes the results
 * to the given {@link BuildListener}.
 * @author Johannes Utzig
 *
 */
public class ProcessStreamLogger extends Thread {
	
	private Process process;
	private BuildListener listener;
	
	public ProcessStreamLogger(Process process, BuildListener listener) {
		super();
		this.process = process;
		this.listener = listener;
	}

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		String result;
		try {
			while ((result = reader.readLine()) != null) {
				listener.getLogger().println(result);
			}
		} catch (IOException e) {
			listener.getLogger().println(e.toString());
			e.printStackTrace(listener.getLogger());
		}
	}
	

}
