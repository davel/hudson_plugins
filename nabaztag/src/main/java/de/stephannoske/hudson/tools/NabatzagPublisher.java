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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import net.sf.json.JSONObject;

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
		public String nabatzagFailTTS = "Build Failed in Hudson ";
		public String nabatzagSuccessTTS = "Build was successfull in Hudson ";
		public String nabatzagRecoverTTS = "Hudson Build ist back to normal";
		public boolean reportOnSucess = false;
	
		protected DescriptorImpl() {
		    super(NabatzagPublisher.class);
		    load();
		}
	
		public boolean configure(final StaplerRequest req, JSONObject json)
		throws FormException {
		    // to persist global configuration information,
		    // set that to properties and call save().
		    nabatzagVoice = req.getParameter("nabatzagVoice");
		    nabatzagSerial = req.getParameter("nabatzagSerial");
		    nabatzagUrl = req.getParameter("nabatzagUrl");
		    nabatzagToken = req.getParameter("nabatzagToken");
		    String reportOnSucessParameter = req.getParameter("reportOnSucess");
		    reportOnSucess = reportOnSucessParameter != null && reportOnSucessParameter.equals("on");
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
	
		return buf.toString().replace(" ", "%20");
    }

    public Descriptor<Publisher> getDescriptor() {
    	return DESCRIPTOR;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build,
	    final Launcher launcher, final BuildListener listener)
    throws InterruptedException, IOException {
		final String name = " " + build.getProject().getName() + " "
		+ build.getNumber();
		String msg;
	
		// Build FAILURE
		if ((build.getResult() == Result.FAILURE)
			|| (build.getResult() == Result.UNSTABLE)) {
		    log.finest("Nabaztag FAILURE");
		    msg = DESCRIPTOR.getNabatzagFailTTS() + name;
		    sendRequest(msg, DESCRIPTOR.getNabatzagFAILDpos(), listener);
		}
	
		// Build RECOVER
		if ((build.getResult() == Result.SUCCESS)
			&& (build.getPreviousBuild() != null)
			&& (build.getPreviousBuild().getResult() == Result.FAILURE)) {
		    // Build RECOVERY
		    log.finest("Nabaztag Build RECOVERY");
		    msg = DESCRIPTOR.getNabatzagRecoverTTS() + name;
		    sendRequest(msg, DESCRIPTOR.getNabatzagSUSSCEEDpos(), listener);
		}
	
		// Build SUCCESS
		if (DESCRIPTOR.reportOnSucess && (build.getResult() == Result.SUCCESS)) {
		    msg = DESCRIPTOR.getNabatzagSuccessTTS() + name;
		    if (build.getPreviousBuild().getResult() == Result.FAILURE) {
	
			log.finest("Nabaztag Build RECOVERY");
			msg = DESCRIPTOR.getNabatzagRecoverTTS() + name;
		    }
		    sendRequest(msg, DESCRIPTOR.getNabatzagSUSSCEEDpos(), listener);
		}
	
		return true;
    }

    /**
     * @param message
     * @param earpos
     * @param listener 
     */
    private void sendRequest(final String message, final String earpos, BuildListener listener) {
		final String requestString = buildRequest(message, earpos);
		log.finest(" sending nabatztag request : " + requestString);
	
	    URLConnection cnx = null;
	    InputStream inputStream = null;
	    BufferedReader bufferedReader = null;
	    try {
	    	cnx = ProxyConfiguration.open(new URL(requestString));
	    	cnx.connect();
	    	inputStream = cnx.getInputStream();
	    	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	    	StringBuilder result = new StringBuilder();
	        String strLine;
			while ((strLine = bufferedReader.readLine()) != null)
				result.append(strLine);
			log.finest("API call result : " + result.toString());
			listener.getLogger().println("Nabaztag has been sucessfully notified.");
	    } catch (MalformedURLException dontCare) {
	    	log.log(Level.WARNING, "URL is malformed.", dontCare);
			listener.error("Unable to build a valid Nabaztag API call.");
		} catch (IOException notImportant) {
	    	log.log(Level.WARNING, "IOException while reading API call result.", notImportant);
			listener.error("Nabaztag has not been sucessfully notified.");
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

}
