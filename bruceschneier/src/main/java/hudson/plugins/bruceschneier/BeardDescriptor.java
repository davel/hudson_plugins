package hudson.plugins.bruceschneier;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

@Extension
public final class BeardDescriptor extends BuildStepDescriptor<Publisher> {

	public BeardDescriptor() {
		super(CordellWalkerRecorder.class);
	}

	@Override
	public String getDisplayName() {
		return "Activate Bruce Schneier";
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> clazz) {
		return true;
	}
}
