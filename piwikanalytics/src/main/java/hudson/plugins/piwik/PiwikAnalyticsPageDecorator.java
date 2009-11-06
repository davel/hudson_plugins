package hudson.plugins.piwik;

import hudson.Extension;
import hudson.Util;
import hudson.model.PageDecorator;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PiwikAnalyticsPageDecorator extends PageDecorator {

	private String siteId;
	private String piwikServer;
	private String piwikPath;
	private String additionnalDownloadExtensions;

	public PiwikAnalyticsPageDecorator() {
		super(PiwikAnalyticsPageDecorator.class);
		load();
	}

	@DataBoundConstructor
	public PiwikAnalyticsPageDecorator(String _siteId, String _piwikServer,
			String _piwikPath, String _additionnalDEx) {
		this();
		this.siteId = _siteId;
		this.piwikServer = _piwikServer;
		this.piwikPath = _piwikPath;
		this.additionnalDownloadExtensions = _additionnalDEx;
	}

	@Override
	public String getDisplayName() {
		return "Piwik Analytics";
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json)
			throws FormException {
		req.bindJSON(this, json);
		save();
		return true;
	}

	public String getSiteId() {
		return Util.fixEmpty(siteId);
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getPiwikServer() {
		return Util.fixEmpty(piwikServer);
	}

	public void setPiwikServer(String piwikServer) {
		this.piwikServer = piwikServer;
	}

	public String getPiwikPath() {
		return Util.fixEmpty(piwikPath);
	}

	public void setPiwikPath(String piwikPath) {
		this.piwikPath = piwikPath;
	}

	public String getAdditionnalDownloadExtensions() {
		return additionnalDownloadExtensions;
	}

	public void setAdditionnalDownloadExtensions(
			String additionnalDownloadExtensions) {
		this.additionnalDownloadExtensions = additionnalDownloadExtensions;
	}

}
