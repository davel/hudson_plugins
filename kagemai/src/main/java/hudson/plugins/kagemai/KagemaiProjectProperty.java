package hudson.plugins.kagemai;

import hudson.Extension;
import hudson.Util;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author yamkazu
 */
public class KagemaiProjectProperty extends JobProperty<Job<?, ?>> {

	private String siteName;
	private String projectId;
	private String regex;
	private boolean linkEnabled;

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public KagemaiProjectProperty(String siteName, String projectId,
			String regex, boolean linkEnabled) {
		String name = siteName;
		if (siteName == null) {
			final KagemaiSite[] sites = DESCRIPTOR.getSites();
			if (sites.length > 0) {
				name = sites[0].getName();
			}
		}
		this.siteName = Util.fixEmptyAndTrim(name);
		this.projectId = Util.fixEmptyAndTrim(projectId);
		this.regex = Util.fixEmptyAndTrim(regex);
		this.linkEnabled = linkEnabled;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getRegex() {
		if (StringUtils.isEmpty(regex)) {
			this.regex = "\\b[0-9.]*[0-9]\\b";
		}
		return regex;
	}

	public boolean isLinkEnabled() {
		return linkEnabled;
	}

	public KagemaiSession getKagemaiSession() {
		return new KagemaiSession(getSite().getBaseUrl(), projectId, getSite()
				.getBasicUserName(), getSite().getBasicPassword(), getSite()
				.getEncode());
	}

	public KagemaiSite getSite() {
		KagemaiSite[] sites = DESCRIPTOR.getSites();
		if (siteName == null && sites.length > 0) {
			return sites[0];
		}
		for (final KagemaiSite site : sites) {
			if (site.getName().equals(siteName)) {
				return site;
			}
		}
		return null;
	}

	/**
	 * @see hudson.model.JobProperty#getDescriptor()
	 */
	@Override
	public JobPropertyDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	/**
	 * @author yamkazu
	 * 
	 */
	public static final class DescriptorImpl extends JobPropertyDescriptor {

		private final CopyOnWriteList<KagemaiSite> sites = new CopyOnWriteList<KagemaiSite>();

		DescriptorImpl() {
			super(KagemaiProjectProperty.class);
			load();
		}

		@Override
		public String getDisplayName() {
			return "Kagemai";
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {
			KagemaiProjectProperty kagemaiProjectProperty = req.bindParameters(
					KagemaiProjectProperty.class, "kagemai.");
			if (kagemaiProjectProperty.siteName == null) {
				kagemaiProjectProperty = null;
			}
			return kagemaiProjectProperty;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			sites.replaceBy(req.bindParametersToList(KagemaiSite.class,
					"kagemai."));
			save();
			return true;
		}

		public KagemaiSite[] getSites() {
			return sites.toArray(new KagemaiSite[0]);
		}

		public FormValidation doRegexCheck(@QueryParameter String value) {
			String regex = Util.fixEmpty(value);
			if (regex == null) {
				return FormValidation.ok();
			}
			try {
				Pattern.compile(regex);
				return FormValidation.ok();
			} catch (PatternSyntaxException e) {
				return FormValidation.error(Messages.error_regex());
			}
		}

		public FormValidation doLoginCheck(@QueryParameter String baseUrl,
				@QueryParameter String basicUserName, @QueryParameter String basicPassword)
				throws IOException {
			//if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return FormValidation.ok();
			baseUrl = Util.fixEmpty(baseUrl);
			basicUserName = Util.fixEmpty(basicUserName);
			basicPassword = Util.fixEmpty(basicPassword);

			if (StringUtils.isEmpty(baseUrl)) {
				return FormValidation.ok();
			}
			KagemaiSession session = new KagemaiSession(
					new URL(baseUrl), basicUserName, basicPassword);
			if (session.isConnect()) {
				return FormValidation.ok();
			} else {
				return FormValidation.error(Messages.error_login());
			}
		}
	}
}
