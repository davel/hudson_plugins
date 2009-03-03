package hudson.plugins.kagemai;

import static org.apache.commons.lang.StringUtils.isEmpty;
import hudson.plugins.kagemai.model.KagemaiIssue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.oro.text.perl.Perl5Util;

/**
 * @author yamkazu
 * 
 */
public class KagemaiSession {

	public static final String LINK_FORMAT = "<a href='%s?project=%s&action=view_report&id=%s'>";
	public static final String LINK_FORMAT_WITH_TOOLTIP = "<a href='%s?project=%s&action=view_report&id=%s' tooltip='%s'>";

	private static final String TITLE_REGEX = "/<h1>.+?:(.*)</h1>/";

	private URL baseUrl;
	private String basicUserName;
	private String basicPassword;
	private String projectId;
	private String encode;

	private HttpClient client;

	public KagemaiSession(URL baseUrl, String basicUserName,
			String basicPassword) {
		this.baseUrl = baseUrl;
		this.basicUserName = basicUserName;
		this.basicPassword = basicPassword;
	}

	public boolean isConnect() {
		boolean result = false;

		client = new HttpClient();
		PostMethod method = new PostMethod(baseUrl.toExternalForm());

		if (isAuthentication()) {
			setAuthentication();
			method.setDoAuthentication(true);
		}

		try {
			if (client.executeMethod(method) == HttpStatus.SC_OK) {
				result = true;
			}
		} catch (HttpException e) {
			LOGGER.log(Level.WARNING, "can not connect kagemai site", e);
		} catch (IOException e) {
			LOGGER
					.log(Level.WARNING, "error in kagemai response processing",
							e);
		} finally {
			method.releaseConnection();
		}

		return result;
	}

	public KagemaiSession(URL baseUrl, String projectId, String basicUserName,
			String basicUserPassword, String encode) {
		this.baseUrl = baseUrl;
		this.basicUserName = basicUserName;
		this.basicPassword = basicUserPassword;
		this.projectId = projectId;
		this.encode = encode;
		initClient();
	}

	private void initClient() {
		client = new HttpClient();
		if (isAuthentication()) {
			setAuthentication();
		}
	}

	private void setAuthentication() {
		int port;
		if (baseUrl.getPort() == -1) {
			port = baseUrl.getDefaultPort();
		} else {
			port = baseUrl.getPort();
		}
		client.getState().setCredentials(
				new AuthScope(baseUrl.getHost(), port),
				new UsernamePasswordCredentials(basicUserName, basicPassword));
	}

	private PostMethod createMethod() {
		PostMethod method = new PostMethod(baseUrl.toExternalForm());
		if (isAuthentication()) {
			method.setDoAuthentication(true);
		}
		method.setParameter("project", projectId);
		method.setParameter("action", "view_report");
		return method;
	}

	private boolean isAuthentication() {
		return isEmpty(basicUserName) == false
				&& isEmpty(basicPassword) == false;
	}

	public List<KagemaiIssue> getIssuesMap(HashSet<Integer> bugIds) {

		if (bugIds.size() == 0) {
			return null;
		}

		List<KagemaiIssue> result = new ArrayList<KagemaiIssue>();
		for (Integer i : bugIds) {
			PostMethod method = null;
			try {
				method = createMethod();
				method.setParameter("id", String.valueOf(i));
				if (client.executeMethod(method) == HttpStatus.SC_OK) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(method
									.getResponseBodyAsStream(), encode));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line).append("\n");
					}
					String summary = getSummary(sb.toString());
					if (isEmpty(summary) == false) {
						result.add(new KagemaiIssue(i, summary));
					}
				}
			} catch (HttpException e) {
				LOGGER.log(Level.WARNING, "can not connect kagemai site", e);
				continue;
			} catch (IOException e) {
				LOGGER.log(Level.WARNING,
						"error in kagemai response processing", e);
				continue;
			} finally {
				method.releaseConnection();
			}
		}
		return result;
	}

	private static String getSummary(String body) {
		String result = null;
		Perl5Util util = new Perl5Util();
		if (util.match(TITLE_REGEX, body)) {
			result = util.group(1);
		}
		return result;
	}

	private static final Logger LOGGER = Logger.getLogger(KagemaiSession.class
			.getName());
}
