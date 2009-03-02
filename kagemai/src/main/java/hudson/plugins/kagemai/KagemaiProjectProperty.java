package hudson.plugins.kagemai;

import hudson.Util;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.CopyOnWriteList;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author yamkazu
 */
public class KagemaiProjectProperty extends JobProperty<Job<?, ?>> {

	private String siteName;
	private String projectId;
	private String regex;
	private boolean linkEnabled;

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
		public boolean configure(StaplerRequest req) throws FormException {
			sites.replaceBy(req.bindParametersToList(KagemaiSite.class,
					"kagemai."));
			save();
			return true;
		}

		public KagemaiSite[] getSites() {
			return sites.toArray(new KagemaiSite[0]);
		}

		public void doRegexCheck(final StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException {
			new FormFieldValidator(req, rsp, false) {
				@Override
				protected void check() throws IOException, ServletException {
					String regex = Util.fixEmpty(request.getParameter("value"));
					if (regex == null) {
						ok();
						return;
					}
					try {
						Pattern.compile(regex);
						ok();
						return;
					} catch (PatternSyntaxException e) {
						error(Messages.error_regex());
						return;
					}
				}
			}.process();
		}

		public void doLoginCheck(final StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException {
			new FormFieldValidator(req, rsp, false) {
				@Override
				protected void check() throws IOException, ServletException {
					String baseUrl = Util.fixEmpty(request
							.getParameter("baseUrl"));
					String basicUserName = Util.fixEmpty(request
							.getParameter("basicUserName"));
					String basicPassword = Util.fixEmpty(request
							.getParameter("basicPassword"));

					KagemaiSession session = new KagemaiSession(
							new URL(baseUrl), basicUserName, basicPassword);
					if (session.isConnect()) {
						ok();
						return;
					} else {
						error(Messages.error_login());
						return;
					}
				}
			}.process();
		}
	}
}