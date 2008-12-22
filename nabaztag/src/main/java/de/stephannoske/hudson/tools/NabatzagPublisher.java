package de.stephannoske.hudson.tools;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author snoske
 * 
 */
public class NabatzagPublisher extends Publisher {

	/**
	 * the DESCRIPTOR
	 */
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	/**the Logger
	 * 
	 */
	private static Logger log = Logger.getLogger(NabatzagPublisher.class);
	
	@DataBoundConstructor
	public NabatzagPublisher() {
		super();

	}

	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	/* (non-Javadoc)
	 * @see hudson.tasks.Publisher#prebuild(hudson.model.Build, hudson.model.BuildListener)
	 */
	@Override
	public boolean prebuild(Build build, BuildListener listener) {
		return super.prebuild(build, listener);
	}

	/* (non-Javadoc)
	 * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		String name = " "+build.getProject().getName() + " "+build.getNumber();
		String msg;

		if (build.getResult() == Result.FAILURE) {
			log.debug(" nabaztag FAILURE");
			msg = DESCRIPTOR.getNabatzagFailTTS() + name;
			sendRequest(msg, DESCRIPTOR.getNabatzagFAILDpos());
		}

		if (build.getResult() == Result.SUCCESS || build.getResult() == Result.UNSTABLE) {
			msg = DESCRIPTOR.getNabatzagSuccessTTS() + name;
			if (build.getPreviousBuild().getResult() == Result.FAILURE) {
				// Build RECOVERY
				log.debug(" nabaztag Build RECOVERY");
				msg = DESCRIPTOR.getNabatzagrecoverTTS() + name;
			}
			sendRequest(msg, DESCRIPTOR.getNabatzagFAILDpos());
		}

		return true;
	}

	public static final class DescriptorImpl extends Descriptor<Publisher> {
		private static final Logger LOGGER = Logger
				.getLogger(DescriptorImpl.class.getName());

		private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays
				.asList("", "(Default)", "(System Default)");

		// 1200331012
		public String nabatzagToken = "Please configure me";
		// 0013D380FDD9
		public String nabatzagSN = "Please configure me";
		public String nabatzagUrl = "http://api.nabaztag.com/vl/FR/api.jsp";
		public String nabatzagVoice = "lea22s";
		public String nabatzagFAILDpos = "posright=6&posleft=6&ears=ok&ttl=600";
		public String nabatzagSUSSCEEDpos = "posright=3&posleft=3&ears=ok&ttl=600";
		public String nabatzagFailTTS = "Build Failed in Hudson ";
		public String nabatzagSuccessTTS = "Build was successfull in Hudson ";
		public String nabatzagrecoverTTS = "Hudson Build ist back to normal";

		protected DescriptorImpl() {
			super(NabatzagPublisher.class);
			load();
		}

		@Override
		public String getDisplayName() {
			return "Nabatzag Publisher ";
		}

		public void setNabatzagToken(String nabatzagToken) {
			this.nabatzagToken = nabatzagToken;
		}

		public void setNabatzagSN(String nabatzagSN) {
			this.nabatzagSN = nabatzagSN;
		}

		public void setNabatzagUrl(String nabatzagUrl) {
			this.nabatzagUrl = nabatzagUrl;
		}

		public void setNabatzagVoice(String nabatzagVoice) {
			this.nabatzagVoice = nabatzagVoice;
		}

		public void setNabatzagFAILDpos(String nabatzagFAILDpos) {
			this.nabatzagFAILDpos = nabatzagFAILDpos;
		}

		public void setNabatzagSUSSCEEDpos(String nabatzagSUSSCEEDpos) {
			this.nabatzagSUSSCEEDpos = nabatzagSUSSCEEDpos;
		}

		public void setNabatzagFailTTS(String nabatzagFailTTS) {
			this.nabatzagFailTTS = nabatzagFailTTS;
		}

		public void setNabatzagSuccessTTS(String nabatzagSuccessTTS) {
			this.nabatzagSuccessTTS = nabatzagSuccessTTS;
		}

		public void setNabatzagrecoverTTS(String nabatzagrecoverTTS) {
			this.nabatzagrecoverTTS = nabatzagrecoverTTS;
		}

		public String getNabatzagFAILDpos() {
			return nabatzagFAILDpos;
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

		public String getNabatzagSN() {
			return nabatzagSN;
		} 

		public String getNabatzagVoice() {
			return nabatzagVoice;
		}

		public String getNabatzagFailTTS() {
			return nabatzagFailTTS;
		}

		public String getNabatzagSuccessTTS() {
			return nabatzagSuccessTTS;
		}

		public String getNabatzagrecoverTTS() {
			return nabatzagrecoverTTS;
		}

		public boolean configure(HttpServletRequest req) throws FormException {
			// to persist global configuration information,
			// set that to properties and call save().
			nabatzagVoice = req.getParameter("nabatzagVoice");
			this.nabatzagSN = req.getParameter("nabaztagSN");
			nabatzagUrl = req.getParameter("nabatzagUrl");
			nabatzagToken = req.getParameter("nabatzagToken");
			// nabatzagFAILDpos = req.getParameter("nabatzagFAILDpos");
			// nabatzagSUSSCEEDpos = req.getParameter("nabatzagSUSSCEEDpos");

			save();
			return super.configure(req);
		}

	}

	/**
	 * @param message
	 * @param earpos
	 */
	private void sendRequest(String message, String earpos) {
		String requestString = buildRequest(message, earpos);
		log.info(" sending nabatztag request : " + requestString);
		HttpClient client = new HttpClient();

		GetMethod method = new GetMethod(requestString);

		try {
			synchronized (this) {
				client.executeMethod(method);
				String result = method.getResponseBodyAsString();
				log.info(" API call result : " + result);
			}

		} catch (Exception e) {
			e.getMessage();
		} finally {
			method.releaseConnection();
		}

	}

	/**
	 * @param message
	 * @param earpos
	 * @return
	 */
	private String buildRequest(String message, String earpos) {
		StringBuffer buf = new StringBuffer();
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

}
