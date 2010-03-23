package de.stephannoske.hudson.tools;

import hudson.Extension;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author snoske
 * @author eric.lemerdy
 */
public class NabatzagPublisher extends Notifier {

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public String nabatzagToken = "";
		public String nabatzagSerial = "";
		public String nabatzagUrl = "http://api.nabaztag.com/vl/FR/api.jsp";
		public String nabatzagVoice = "UK-Penelope";
		public String nabatzagFAILDpos = "posright=8&posleft=8&ears=ok";
		public String nabatzagSUSSCEEDpos = "posright=0&posleft=0&ears=ok";
		public String nabatzagBUILDEDpos = "posright=4&posleft=12&ears=ok";
		public String nabatzagFailTTS = "Failure of build \"${buildNumber}\" in project \"${projectName}\".";
		public String nabatzagSuccessTTS = "Success of build \"${buildNumber}\" in project \"${projectName}\".";
		public String nabatzagRecoverTTS = "Project \"${projectName}\" recovered at build \"${buildNumber}\".";
		public String nabatzagBuildTTS = "Build \"${buildNumber}\" of project \"${projectName}\" has started.";
		public boolean reportOnSucess = false;
		public boolean notifyOnBuildStart = false;
	
		protected DescriptorImpl() {
		    super(NabatzagPublisher.class);
		    load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public boolean configure(final StaplerRequest req, JSONObject json)
				throws FormException {
		    nabatzagVoice = req.getParameter("nabatzagVoice");
		    nabatzagSerial = req.getParameter("nabatzagSerial");
		    nabatzagUrl = req.getParameter("nabatzagUrl");
		    nabatzagToken = req.getParameter("nabatzagToken");
		    reportOnSucess = "on".equals(req.getParameter("reportOnSucess"));
			nabatzagFailTTS = req.getParameter("nabatzagFailTTS");
			nabatzagSuccessTTS = req.getParameter("nabatzagSuccessTTS");
			nabatzagRecoverTTS = req.getParameter("nabatzagRecoverTTS");
			nabatzagBuildTTS = req.getParameter("nabatzagBuildTTS");
			notifyOnBuildStart = "on".equals(req.getParameter("nabatzagNotifyOnBuildStart"));

		    save();
		    return super.configure(req, json);
		}
	
		@Override
		public String getDisplayName() {
		    return "Nabatzag Publisher ";
		}
		
		public String getNabatzagFAILDpos() {
		    return nabatzagFAILDpos;
		}
	
		public String getNabatzagFailTTS() {
		    return nabatzagFailTTS;
		}
	
		public String getNabatzagRecoverTTS() {
		    return nabatzagRecoverTTS;
		}
	
		public String getNabatzagSerial() {
		    return nabatzagSerial;
		}
	
		public String getNabatzagSuccessTTS() {
		    return nabatzagSuccessTTS;
		}
	
		public String getNabatzagSUSSCEEDpos() {
		    return nabatzagSUSSCEEDpos;
		}

		public String getNabatzagBuildTTS() {
		    return nabatzagBuildTTS;
		}

		public String getNabatzagBUILDEDpos() {
		    return nabatzagBUILDEDpos;
		}
	
		public String getNabatzagToken() {
		    return nabatzagToken;
		}
	
		public String getNabatzagUrl() {
		    return nabatzagUrl;
		}
	
		public String getNabatzagVoice() {
		    return nabatzagVoice;
		}
	
		public boolean isReportOnSucess() {
		    return reportOnSucess;
		}
	
		public void setNabatzagFAILDpos(final String nabatzagFAILDpos) {
		    this.nabatzagFAILDpos = nabatzagFAILDpos;
		}
	
		public void setNabatzagFailTTS(final String nabatzagFailTTS) {
		    this.nabatzagFailTTS = nabatzagFailTTS;
		}
	
		public void setNabatzagRecoverTTS(final String nabatzagRecoverTTS) {
		    this.nabatzagRecoverTTS = nabatzagRecoverTTS;
		}
	
		public void setNabatzagSerial(final String nabatzagSerial) {
		    this.nabatzagSerial = nabatzagSerial;
		}
	
		public void setNabatzagSuccessTTS(final String nabatzagSuccessTTS) {
		    this.nabatzagSuccessTTS = nabatzagSuccessTTS;
		}
	
		public void setNabatzagSUSSCEEDpos(final String nabatzagSUSSCEEDpos) {
		    this.nabatzagSUSSCEEDpos = nabatzagSUSSCEEDpos;
		}

		public void setNabatzagBuildTTS(final String nabatzagBuildTTS) {
		    this.nabatzagBuildTTS = nabatzagBuildTTS;
		}

		public void setNabatzagBUILDEDpos(final String nabatzagBUILDEDpos) {
		    this.nabatzagBUILDEDpos = nabatzagBUILDEDpos;
		}
	
		public void setNabatzagToken(final String nabatzagToken) {
		    this.nabatzagToken = nabatzagToken;
		}
	
		public void setNabatzagUrl(final String nabatzagUrl) {
		    this.nabatzagUrl = nabatzagUrl;
		}
	
		public void setNabatzagVoice(final String nabatzagVoice) {
		    this.nabatzagVoice = nabatzagVoice;
		}
	
		public void setReportOnSucess(final boolean reportOnSucess) {
		    this.reportOnSucess = reportOnSucess;
		}

		public boolean isNotifyOnBuildStart() {
			return notifyOnBuildStart;
		}

		public void setNotifyOnBuildStart(boolean notifyOnBuildStart) {
			this.notifyOnBuildStart = notifyOnBuildStart;
		}

		public FormValidation doTestCredentials(@QueryParameter String nabatzagSerial, @QueryParameter String nabatzagToken)
				throws IOException, ServletException {
			String requestString = buildRequestWithAWakeUpAction(nabatzagSerial, nabatzagToken);
			log.finest(" sending nabatztag request : " + requestString);
			URLConnection cnx = ProxyConfiguration.open(new URL(requestString));
			cnx.connect();
			InputStream inputStream = cnx.getInputStream();
			String result = IOUtils.toString(inputStream);
			log.finest("API call result : " + result);
			Map<String, String> messages = parseAndExtractMessages(result);
			if (messages.containsKey("COMMANDSENT")) {
				return FormValidation.ok("Credentials are valid and your rabbit is awake.");
			}
			if (messages.containsKey("NOGOODTOKENORSERIAL")) {
				return FormValidation.error(messages.get("NOGOODTOKENORSERIAL"));
			}
			if (messages.containsKey("NOGOODSERIAL")) {
				return FormValidation.error(messages.get("NOGOODSERIAL"));
			}
			return FormValidation.error("Unexpected API result: " + messages.toString());
		}

    }

    /**
     * the DESCRIPTOR
     */
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /** the Logger */
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(NabatzagPublisher.class.getName());

    @DataBoundConstructor
    public NabatzagPublisher() {
    	super();
    }

    private String buildRequestWithAMessageAVoiceAndSomeEarPosition(final String message, final String earpos) {
		final StringBuilder buf = buildFirstCommonRequest(DESCRIPTOR.getNabatzagSerial(), DESCRIPTOR.getNabatzagToken());
		buf.append("tts=").append(message);
		buf.append("&");
		buf.append("voice=").append(DESCRIPTOR.getNabatzagVoice());
		buf.append("&");
		buf.append(StringUtils.defaultString(earpos));
		return buf.toString();
    }

	private static StringBuilder buildFirstCommonRequest(String serialNumber, String token) {
		final StringBuilder buf = new StringBuilder();
		buf.append(DESCRIPTOR.getNabatzagUrl()).append("?");
		buf.append("sn=").append(serialNumber);
		buf.append("&");
		buf.append("token=").append(token);
		buf.append("&");
		return buf;
	}
	
	protected static String buildRequestWithAWakeUpAction(String serialNumber, String token) {
		final StringBuilder buf = buildFirstCommonRequest(serialNumber, token);
		buf.append("action=14");
		return buf.toString();
	}

	@Override
    public boolean prebuild(final AbstractBuild<?, ?> build, BuildListener listener) {
    	if (DESCRIPTOR.isNotifyOnBuildStart()) {
	    	String msg = DESCRIPTOR.getNabatzagBuildTTS();
	        log.finest("Nabaztag Build BEGIN");
	        sendRequest(msg, DESCRIPTOR.getNabatzagBUILDEDpos(), build, listener);
    	}
        return true;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build,
	    final Launcher launcher, final BuildListener listener)
    throws InterruptedException, IOException {
    	if (!isSNAndTokenDefined()) {
			listener.getLogger().println("Nabaztag Serial Number or Token are not defined, notification has not been sent.");
    		return false;
    	}
		String msg;
		
		// Build FAILURE
		if ((build.getResult() == Result.FAILURE)
				|| (build.getResult() == Result.UNSTABLE)) {
			msg = DESCRIPTOR.getNabatzagFailTTS();
		    log.finest("Nabaztag Build FAILURE");
		    sendRequest(msg, DESCRIPTOR.getNabatzagFAILDpos(), build, listener);
		} else if (build.getResult() == Result.SUCCESS) {
			
			// Build RECOVERY
			if (build.getPreviousBuild() != null
					&& build.getPreviousBuild().getResult() == Result.FAILURE) {
				msg = DESCRIPTOR.getNabatzagRecoverTTS();
				log.finest("Nabaztag Build RECOVERY");
			    sendRequest(msg, DESCRIPTOR.getNabatzagSUSSCEEDpos(), build, listener);
			}

			// Build SUCCESS
			else if (DESCRIPTOR.reportOnSucess) {
				msg = DESCRIPTOR.getNabatzagSuccessTTS();
				log.finest("Nabaztag Build SUCCESS");
			    sendRequest(msg, DESCRIPTOR.getNabatzagSUSSCEEDpos(), build, listener);
			} else {
				listener.getLogger().println("User has choosen not to be notified of success, notification has not been sent.");
			}
		} else {
			listener.getLogger().println("Build result not handled by Nabaztag notifier, notification has not been sent.");
		}
	
		return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    private boolean isSNAndTokenDefined() {
    	return StringUtils.isNotBlank(DESCRIPTOR.nabatzagSerial) && StringUtils.isNotBlank(DESCRIPTOR.nabatzagSerial);
	}

	/**
     * @param message
     * @param earpos
	 * @param build 
     * @param listener 
	 * @param build 
     */
    private void sendRequest(final String message, final String earpos, AbstractBuild<?,?> build, BuildListener listener) {
    	String substituedMessage = StringUtils.replaceEach(
    			message,
    			new String[]{"${projectName}", "${buildNumber}"},
    			new String[]{build.getProject().getName(), String.valueOf(build.getNumber())}
    	);
    	String urlEncodedMessage = null;
	    URLConnection cnx = null;
	    InputStream inputStream = null;
	    try {
	    	urlEncodedMessage = URLEncoder.encode(substituedMessage, "UTF-8");
			String requestString = buildRequestWithAMessageAVoiceAndSomeEarPosition(urlEncodedMessage, earpos);
			log.finest(" sending nabatztag request : " + requestString);
	    	cnx = ProxyConfiguration.open(new URL(requestString));
	    	cnx.connect();
	    	inputStream = cnx.getInputStream();
	    	String result = IOUtils.toString(inputStream);
			log.finest("API call result : " + result.toString());
			analyseResult(result.toString(), listener,
					new ArrayList<String>(Arrays.asList(new String[]{"EARPOSITIONSENT","POSITIONEAR","TTSSENT"})));
	    } catch (UnsupportedEncodingException notFatal) {
	    	log.log(Level.WARNING, "URL is malformed.", notFatal);
			listener.error("Unable to url encode the Nabaztag message.");
	    } catch (MalformedURLException dontCare) {
	    	log.log(Level.WARNING, "URL is malformed.", dontCare);
			listener.error("Unable to build a valid Nabaztag API call.");
		} catch (IOException notImportant) {
	    	log.log(Level.WARNING, "IOException while reading API call result.", notImportant);
			listener.error("Nabaztag has not been successfully notified.");
		} finally {
			IOUtils.closeQuietly(inputStream);
	    }
    }

	protected void analyseResult(String contentResult, BuildListener listener, List<String> expectedCommands) {
		List<String> unExpectedCommands = new ArrayList<String>();
		for (String message : parseAndExtractMessages(contentResult).keySet()) {
			if (expectedCommands.contains(message)) {
				expectedCommands.remove(message);
			} else {
				unExpectedCommands.add(message);
			}
		}

		boolean success = true;
		StringBuilder out = new StringBuilder();
		if (!expectedCommands.isEmpty()) {
			success = false;
			out.append("Following expected confirmations has not been received: ");
			out.append(expectedCommands);
			out.append("\n");
		}
		if (!unExpectedCommands.isEmpty()) {
			success = false;
			out.append("Following unexpected messages has been received: ");
			out.append(unExpectedCommands);
			out.append(". ");
		}
		if (success) {
			listener.getLogger().println("Nabaztag has been successfully notified.");
		} else {
			listener.getLogger().println("Nabaztag has not been successfully notified: ");
			listener.getLogger().println(out);
		}
	}
	
	private static Map<String, String> parseAndExtractMessages(String contentResult) {
		Map<String, String> messages = new HashMap<String, String>();
		String currentElementText = null;
		XMLStreamReader xmlStreamReader;
		try {
			xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(contentResult));
			while (xmlStreamReader.hasNext()) {
				int next = xmlStreamReader.next();
				if (next == XMLStreamConstants.START_ELEMENT) {
					String currentElement = xmlStreamReader.getName().getLocalPart();
					if (currentElement.equals("message")) {
						currentElementText = xmlStreamReader.getElementText();
						messages.put(currentElementText, "");
					} else if (currentElement.equals("comment")) {
						messages.put(currentElementText, xmlStreamReader.getElementText());
					}
				}
			}
		} catch (XMLStreamException e) {
	    	log.log(Level.WARNING, "Unable to read xml result.", e);
		} catch (FactoryConfigurationError e) {
	    	log.log(Level.WARNING, "Unable to create xml parser to read xml result.", e);
		}
		return messages;
	}

}
