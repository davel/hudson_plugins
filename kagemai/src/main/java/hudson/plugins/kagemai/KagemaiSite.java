package hudson.plugins.kagemai;

import hudson.Util;
import hudson.model.AbstractProject;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Kagemai Site.
 * 
 * @author yamkazu
 * 
 */
public class KagemaiSite {

	private static final String DEFAULT_ENCODE = "EUC_JP";

	private URL baseUrl;
	private String basicUserName;
	private String basicPassword;
	private String encode;

	@DataBoundConstructor
	public KagemaiSite(String regex, URL baseUrl, String basicUserName,
			String basicPassword, String encode) {
		this.baseUrl = baseUrl;
		this.basicUserName = Util.fixEmptyAndTrim(basicUserName);
		this.basicPassword = Util.fixEmptyAndTrim(basicPassword);
		this.encode = encode;
	}

	public URL getBaseUrl() {
		return baseUrl;
	}

	public String getBasicUserName() {
		return basicUserName;
	}

	public String getBasicPassword() {
		return basicPassword;
	}

	public String getName() {
		return baseUrl.toExternalForm();
	}

	public String getEncode() {
		return StringUtils.isEmpty(encode) ? DEFAULT_ENCODE : encode;
	}

	public static KagemaiSite get(final AbstractProject<?, ?> project) {
		KagemaiProjectProperty mpp = project
				.getProperty(KagemaiProjectProperty.class);
		if (mpp != null) {
			KagemaiSite site = mpp.getSite();
			if (site != null) {
				return site;
			}
		}
		KagemaiSite[] sites = KagemaiProjectProperty.DESCRIPTOR.getSites();
		if (sites.length == 1) {
			return sites[0];
		}
		return null;
	}

}
