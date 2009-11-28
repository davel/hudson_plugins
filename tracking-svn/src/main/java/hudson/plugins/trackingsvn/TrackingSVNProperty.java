package hudson.plugins.trackingsvn;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Run;
import hudson.scm.SubversionSCM;
import hudson.security.AccessControlled;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class TrackingSVNProperty extends JobProperty<AbstractProject<?, ?>> {

	public enum ToTrack {
		LAST_STABLE("Last stable build") {
			@Override
			public Run<?, ?> getBuild(Job<?, ?> project) {
				return project.getLastStableBuild();
			}
		},
		LAST_SUCCESSFUL("Last successful build") {
			@Override
			public Run<?, ?> getBuild(Job<?, ?> project) {
				return project.getLastSuccessfulBuild();
			}
		},
		LAST_BUILD("Last build") {

			@Override
			public Run<?, ?> getBuild(Job<?, ?> project) {
				return project.getLastCompletedBuild();
			}

		},		LAST_FAILED_BUILD("Last failed build") {

			@Override
			public Run<?, ?> getBuild(Job<?, ?> project) {
				return project.getLastFailedBuild();
			}

		};
;
		private String displayValue;

		private ToTrack(String displayValue) {
			this.displayValue = displayValue;
		}

		public abstract Run<?, ?> getBuild(
				Job<?, ?> project);

		public String toString() {
			return displayValue;
		}
	}

	private final String sourceProject;
	private final ToTrack toTrack;

	@DataBoundConstructor
	public TrackingSVNProperty(String sourceProject, ToTrack toTrack) {
		super();
		this.sourceProject = sourceProject;
		this.toTrack = toTrack;
	}

	public String getSourceProject() {
		return sourceProject;
	}

	public ToTrack getToTrack() {
		return toTrack;
	}

	@Extension
	public static class DescriptorImpl extends JobPropertyDescriptor {

		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return Job.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName() {
			return "Track another SVN project";
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {
			if (formData.getJSONObject("track-svn") == null
					|| formData.getJSONObject("track-svn").isNullObject()) {
				return null;
			}

			return super.newInstance(req, formData.getJSONObject("track-svn"));
		}

		/**
		 * Form validation method.
		 */
		public FormValidation doCheckSourceProject(
				@AncestorInPath AccessControlled subject,
				@QueryParameter String value) {
			// Require CONFIGURE permission on this project
			if (!subject.hasPermission(Item.CONFIGURE))
				return FormValidation.ok();

			if (value == null)
				return FormValidation.ok();

			value = value.trim();

			if (value.equals("")) {
				return FormValidation.error("This field is required");
			}

			Item item = Hudson.getInstance().getItem(value);
			if (item == null)
				return FormValidation.error("No such project '" + value
						+ "'. Did you mean '"
						+ AbstractProject.findNearest(value).getName() + "' ?");
			if (item instanceof Job
					&& (((AbstractProject) item).getScm() instanceof SubversionSCM)) {
				return FormValidation.ok();
			}

			return FormValidation
					.error("'" + value + "' is not an SVN project");

		}
	}

}
