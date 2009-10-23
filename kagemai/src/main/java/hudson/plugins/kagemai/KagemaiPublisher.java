package hudson.plugins.kagemai;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.kagemai.model.KagemaiIssue;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author yamkazu
 * 
 */
public class KagemaiPublisher extends Recorder {

	KagemaiPublisher() {
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		KagemaiSite site = KagemaiSite.get(build.getProject());

		if (site == null) {
			build.setResult(Result.FAILURE);
			return true;
		}

		HashSet<Integer> bugIds = new HashSet<Integer>();
		KagemaiProjectProperty mpp = build.getParent().getProperty(
				KagemaiProjectProperty.class);
		if (mpp != null && mpp.getSite() != null) {
			String regex = mpp.getRegex();
			Pattern pattern = Pattern.compile(regex);
			for (Entry entry : build.getChangeSet()) {
				Matcher matcher = pattern.matcher(entry.getMsg());
				while (matcher.find()) {
					try {
						bugIds.add(Integer.valueOf(matcher.group(matcher
								.groupCount())));
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
		} else {
			build.setResult(Result.FAILURE);
			return true;
		}
		KagemaiSession kagemaiSession = mpp.getKagemaiSession();
		List<KagemaiIssue> issues = null;
		if ((!bugIds.isEmpty()) && kagemaiSession != null) {
			issues = kagemaiSession.getIssuesMap(bugIds);
		}
		if (issues != null && issues.size() > 0) {
			Collections.sort(issues);
		}
		KagemaiBuildAction action = new KagemaiBuildAction(build, issues, mpp
				.getSiteName(), mpp.getProjectId());
		build.addAction(action);

		return true;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super(KagemaiPublisher.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.publisher_dispname();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public KagemaiPublisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return new KagemaiPublisher();
		}

	}
}
