package hudson.plugins.buggame;

import java.util.Date;

import net.sf.json.JSONObject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Preconditions;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.plugins.buggame.model.Goal;

public final class ChallengeProperty extends
JobProperty<AbstractProject<?, ?>> {

	/**
	 * This will the URL to the project main branch.
	 */
	private String projectUrl;

	@DataBoundConstructor
	public ChallengeProperty(String projectUrl) {
		//
	}

	@Override
	public Action getJobAction(AbstractProject<?, ?> job) {

		return null;
	}
	/*
@Override
public JobPropertyDescriptor getDescriptor() {
return DESCRIPTOR;
}

public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	 */
	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {

		public DescriptorImpl() {
			super(ChallengeProperty.class);
			load();
		}

		public boolean isApplicable(Class<? extends Job> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}

		public String getDisplayName() {
			return "Github project page";
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {
			ChallengeProperty tpp = req.bindJSON(
					ChallengeProperty.class, formData);
			if (tpp.projectUrl == null) {
			}
			return tpp;
		}
	}
}

