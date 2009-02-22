package de.stephannoske.hudson.tools;

import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author snoske
 * @author eric.lemerdy
 */
public class NabatzagPublisher extends Publisher {

    public static final class DescriptorImpl extends Descriptor<Publisher> {
		public String nabatzagToken = "";
		public String nabatzagSerial = "";
		public String nabatzagUrl = "http://api.nabaztag.com/vl/FR/api.jsp";
		public String nabatzagVoice = "lea22s";
		public String nabatzagFAILDpos = "posright=6&posleft=6&ears=ok&ttl=600";
		public String nabatzagSUSSCEEDpos = "posright=3&posleft=3&ears=ok&ttl=600";
		public String nabatzagFailTTS = "Build Failed in Hudson ${projectName} ${buildNumber}";
		public String nabatzagSuccessTTS = "Build was successfull in Hudson ${projectName} ${buildNumber}";
		public String nabatzagRecoverTTS = "Hudson Build is back to normal ${projectName} ${buildNumber}";
		public boolean reportOnSucess = false;
	
		protected DescriptorImpl() {
		    super(NabatzagPublisher.class);
		    load();
		}
	
		public boolean configure(final StaplerRequest req, JSONObject json)
				throws FormException {
		    nabatzagVoice = req.getParameter("nabatzagVoice");
		    nabatzagSerial = req.getParameter("nabatzagSerial");
		    nabatzagUrl = req.getParameter("nabatzagUrl");
		    nabatzagToken = req.getParameter("nabatzagToken");
		    String reportOnSuccessParameter = req.getParameter("reportOnSucess");
		    reportOnSucess = reportOnSuccessParameter != null && reportOnSuccessParameter.equals("on");
			nabatzagFailTTS = req.getParameter("nabatzagFailTTS");
			nabatzagSuccessTTS = req.getParameter("nabatzagSuccessTTS");
			nabatzagRecoverTTS = req.getParameter("nabatzagRecoverTTS");
	
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
	
		public String getNabatzagSN() {
		    return nabatzagSerial;
		}
	
		public String getNabatzagSuccessTTS() {
		    return nabatzagSuccessTTS;
		}
	
		public String getNabatzagSUSSCEEDpos() {
		    return nabatzagSUSSCEEDpos;
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
	
		public void setNabatzagSN(final String nabatzagSN) {
		    this.nabatzagSerial = nabatzagSN;
		}
	
		public void setNabatzagSuccessTTS(final String nabatzagSuccessTTS) {
		    this.nabatzagSuccessTTS = nabatzagSuccessTTS;
		}
	
		public void setNabatzagSUSSCEEDpos(final String nabatzagSUSSCEEDpos) {
		    this.nabatzagSUSSCEEDpos = nabatzagSUSSCEEDpos;
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

    }

    /**
     * the DESCRIPTOR
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /** the Logger */
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(NabatzagPublisher.class.getName());

    @DataBoundConstructor
    public NabatzagPublisher() {
    	super();
    }

    /**
     * @param message
     * @param earpos
     * @return
     */
    private String buildRequest(final String message, final String earpos) {
		final StringBuffer buf = new StringBuffer();
		buf.append(DESCRIPTOR.getNabatzagUrl() + "?");
		buf.append("sn=" + DESCRIPTOR.getNabatzagSN());
		buf.append("&");
		buf.append("token=" + DESCRIPTOR.getNabatzagToken());
		buf.append("&");
		buf.append("tts=" + message);
		buf.append("&");
		buf.append("voice=" + DESCRIPTOR.getNabatzagVoice());
		buf.append("&");
		buf.append("" + earpos);
		
		return buf.toString();
    }

    public Descriptor<Publisher> getDescriptor() {
    	return DESCRIPTOR;
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
				listener.getLogger().println("User has choosed not to be notified of success, notification has not been sent.");
			}
		} else {
			listener.getLogger().println("Build result not handled by Nabaztag notifier, notification has not been sent.");
		}
	
		return true;
    }

    private boolean isSNAndTokenDefined() {
    	return DESCRIPTOR.nabatzagSerial != null && DESCRIPTOR.nabatzagSerial.length() > 0
    		&& DESCRIPTOR.nabatzagToken != null && DESCRIPTOR.nabatzagToken.length() > 0;
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
	    BufferedReader bufferedReader = null;
	    try {
	    	urlEncodedMessage = URLEncoder.encode(substituedMessage, "UTF-8");
			String requestString = buildRequest(urlEncodedMessage, earpos);
			log.finest(" sending nabatztag request : " + requestString);
	    	cnx = ProxyConfiguration.open(new URL(requestString));
	    	cnx.connect();
	    	inputStream = cnx.getInputStream();
	    	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	    	StringBuilder result = new StringBuilder();
	        String strLine;
			while ((strLine = bufferedReader.readLine()) != null) {
				result.append(strLine);
			}
			log.finest("API call result : " + result.toString());
			analyseResult(result.toString(), listener);
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
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
			    	log.log(Level.WARNING, "IOException while closing API connection.", e);
				}
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "IOException while closing API connection.", e);
				}
	    }
    }

	private void analyseResult(String string, BuildListener listener) {
		List<String> expectedCommands = new ArrayList<String>(3);
		expectedCommands.add("EARPOSITIONSENT");
		expectedCommands.add("POSITIONEAR");
		expectedCommands.add("TTSSENT");
		List<String> unExpectedCommands = new ArrayList<String>();
		
		XMLStreamReader xmlStreamReader;
		try {
			xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(string));
			while (xmlStreamReader.hasNext()) {
				int next = xmlStreamReader.next();
				if (next == XMLStreamConstants.START_ELEMENT) {
					String currentElement = xmlStreamReader.getName().getLocalPart();
					if (currentElement.equals("message")) {
						String elementText = xmlStreamReader.getElementText();
						if (expectedCommands.contains(elementText)) {
							expectedCommands.remove(elementText);
						} else {
							unExpectedCommands.add(elementText);
						}
					}
				}
			}
		} catch (XMLStreamException e) {
	    	log.log(Level.WARNING, "Unable to read xml result.", e);
		} catch (FactoryConfigurationError e) {
	    	log.log(Level.WARNING, "Unable to create xml parser to read xml result.", e);
		}

		boolean success = true;
		StringBuilder out = new StringBuilder();
		if (expectedCommands.size() > 0) {
			success = false;
			out.append("Following expected confirmations has not been received: ");
			out.append(expectedCommands.toString());
			out.append("\n");
		}
		if (unExpectedCommands.size() > 0) {
			success = false;
			out.append("Following unexpected messages has been received: ");
			out.append(unExpectedCommands.toString());
			out.append(". ");
		}
		if (success) {
			listener.getLogger().println("Nabaztag has been successfully notified.");
		} else {
			listener.getLogger().println("Nabaztag has not been successfully notified: ");
			listener.getLogger().println(out.toString());
		}
	}

}
