package de.stephannoske.hudson.tools;

import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.Build;
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
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author snoske
 * 
 */
public class NabatzagPublisher extends Publisher {

    public static final class DescriptorImpl extends Descriptor<Publisher> {
	private static final Logger LOGGER = Logger
	.getLogger(DescriptorImpl.class.getName());

	private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays
	.asList("", "(Default)", "(System Default)");

	// 1200331012
	public String nabatzagToken = "Please configure me";
	// 0013D380FDD9
	public String nabatzagSerial = "Please configure me";
	public String nabatzagUrl = "http://api.nabaztag.com/vl/FR/api.jsp";
	public String nabatzagVoice = "lea22s";
	public String nabatzagFAILDpos = "posright=6&posleft=6&ears=ok&ttl=600";
	public String nabatzagSUSSCEEDpos = "posright=3&posleft=3&ears=ok&ttl=600";
	public String nabatzagFailTTS = "Build Failed in Hudson ";
	public String nabatzagSuccessTTS = "Build was successfull in Hudson ";
	public String nabatzagrecoverTTS = "Hudson Build ist back to normal";
	public boolean reportOnSucess = false;

	protected DescriptorImpl() {
	    super(NabatzagPublisher.class);
	    load();
	}

	public boolean configure(final HttpServletRequest req)
	throws FormException {
	    // to persist global configuration information,
	    // set that to properties and call save().
	    nabatzagVoice = req.getParameter("nabatzagVoice");
	    this.nabatzagSerial = req.getParameter("nabatzagSerial");
	    nabatzagUrl = req.getParameter("nabatzagUrl");
	    nabatzagToken = req.getParameter("nabatzagToken");
	    reportOnSucess = Boolean
	    .valueOf(req.getParameter("reportOnSucess"));
	    // nabatzagFAILDpos = req.getParameter("nabatzagFAILDpos");
	    // nabatzagSUSSCEEDpos = req.getParameter("nabatzagSUSSCEEDpos");

	    save();
	    return super.configure(req);
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

	public String getNabatzagrecoverTTS() {
	    return nabatzagrecoverTTS;
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

	public void setNabatzagrecoverTTS(final String nabatzagrecoverTTS) {
	    this.nabatzagrecoverTTS = nabatzagrecoverTTS;
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

    /**
     * the Logger
     * 
     */
    private static Logger log = Logger.getLogger(NabatzagPublisher.class);

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
     * , hudson.Launcher, hudson.model.BuildListener)
     */
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
	    log.debug(" nabaztag FAILURE");
	    msg = DESCRIPTOR.getNabatzagFailTTS() + name;
	    sendRequest(msg, DESCRIPTOR.getNabatzagFAILDpos());
	}

	// Build RECOVER
	if ((build.getResult() == Result.SUCCESS)
		&& (build.getPreviousBuild().getResult() == Result.FAILURE)) {
	    // Build RECOVERY
	    log.debug(" nabaztag Build RECOVERY");
	    msg = DESCRIPTOR.getNabatzagrecoverTTS() + name;
	    sendRequest(msg, DESCRIPTOR.getNabatzagSUSSCEEDpos());
	}

	// Build SUCCESS
	if (DESCRIPTOR.reportOnSucess && (build.getResult() == Result.SUCCESS)) {
	    msg = DESCRIPTOR.getNabatzagSuccessTTS() + name;
	    if (build.getPreviousBuild().getResult() == Result.FAILURE) {

		log.debug(" nabaztag Build RECOVERY");
		msg = DESCRIPTOR.getNabatzagrecoverTTS() + name;
	    }
	    sendRequest(msg, DESCRIPTOR.getNabatzagSUSSCEEDpos());
	}

	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.Publisher#prebuild(hudson.model.Build,
     * hudson.model.BuildListener)
     */
    @Override
    public boolean prebuild(final Build build, final BuildListener listener) {
	return super.prebuild(build, listener);
    }

    /**
     * @param message
     * @param earpos
     */
    private void sendRequest(final String message, final String earpos) {
	final String requestString = buildRequest(message, earpos);
	log.info(" sending nabatztag request : " + requestString);

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
		log.info(" API call result : " + result.toString());
    } catch (MalformedURLException dontCare) {
    	log.error("URL is malformed.", dontCare);
	} catch (IOException notImportant) {
    	log.error("IOException while reading API call result.", notImportant);
	} finally {
		if (bufferedReader != null)
			try {
				bufferedReader.close();
			} catch (IOException e) {
		    	log.error("IOException while closing API connection.", e);
			}
		if (inputStream != null)
			try {
				inputStream.close();
			} catch (IOException e) {
				log.error("IOException while closing API connection.", e);
			}
    }
	
    }

}
