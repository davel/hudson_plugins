package de.fspengler.hudson.plugin;

import hudson.Util;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.io.IOException;
import java.util.regex.Matcher;

import de.fspengler.hudson.plugin.LogActionProperty.LogActionDescriptor;

public class LogItemListener extends RunListener<Run> {

	@Override
	public void onCompleted(Run r, TaskListener listener) {
		LogActionDescriptor descriptor = LogActionProperty.DESCRIPTOR;
		if (!descriptor.isEnabled()){
			return;
		}
		
		if (r.getResult() == Result.FAILURE
				&& descriptor.isRestartEnabled()
				&& descriptor.getRestartPattern() != null) {
			try {

				String log = Util.loadFile(r.getLogFile(), r.getCharset());
				Matcher matcher = descriptor
						.getPatternForRestart().matcher(log);
				if (matcher.find()) {
					if ( descriptor.getRestartDescription().length() > 0){
						r.setDescription(descriptor.getRestartDescription());
					}
					Project<?, ?> project = Hudson.getInstance()
							.getItemByFullName(r.getParent().getFullName(),
									Project.class);
					project.scheduleBuild(descriptor.getRestartDelay());

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected LogItemListener() {
		super(Run.class);
		
	}

}
