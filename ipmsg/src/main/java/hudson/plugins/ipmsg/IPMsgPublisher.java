package hudson.plugins.ipmsg;

import hudson.Extension;
import hudson.Launcher;
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
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Publisher class for IPMessenger.
 *
 * @author Toyokazu Ohara
 */
public class IPMsgPublisher extends Notifier {
    private static final int DEFAULT_PORT          = 2425;
	private static final int DEFAULT_LOG_LINE_SIZE = 100;
    private static final int MINMUM_PORT_NUMBER    = 0;
    private static final int MAXMUM_PORT_NUMBER    = 65535;
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build.getResult() == Result.FAILURE) {
        	List lines = build.getLog(DESCRIPTOR.getLogLineSize());
        	StringBuilder sb = new StringBuilder();
        	sb.append("fail to build.\n");
        	sb.append("please make sure.\n");
        	sb.append("\n\n");
        	for (int i = 0, iMax = lines.size(); i < iMax; i++) {
        		sb.append(lines.get(i).toString());
        	}
            MsgClient.sendAll(sb.toString());
        }

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
    	private int    port;
        private String userName;
        private String nickName;
        private String group;
		private int    logLineSize = -1;
        public int getPort() {
        	if (port > 0) {
        		return port;
        	} else {
        		return DEFAULT_PORT;
        	}
		}

		public String getUserName() {
			if (userName == null) {
				return "hudson";
			}
			return userName;
		}

		public String getNickName() {
			if (nickName == null) {
				return "hudson";
			}
			return nickName;
		}

		public String getGroup() {
			if (group == null) {
				return "hudson";
			}
			return group;
		}

		public int getLogLineSize() {
			if (logLineSize < 0) {
				return DEFAULT_LOG_LINE_SIZE;
			}
			return logLineSize;
		}

        
        DescriptorImpl() {
            super(IPMsgPublisher.class);
            load();
        }

        public String getDisplayName() {
            return "IPMessenger reports";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public IPMsgPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new IPMsgPublisher();
        }
        
        @Override
        public boolean configure( StaplerRequest req, JSONObject json ) throws FormException {
    		MsgClient.logout();

        	port        = nullify(req.getParameter("ipmsg_port"), -1);
        	userName    = nullify(req.getParameter("ipmsg_userName"));
        	nickName    = nullify(req.getParameter("ipmsg_nickName"));
        	group       = nullify(req.getParameter("ipmsg_group"));
    		logLineSize = nullify(req.getParameter("ipmsg_logLineSize"), -1);

    		MsgClient.login();
    		save();
    		return true;
        }

        /**
         * Checks the Port in <tt>global.jelly</tt>
         */
        public FormValidation doPortCheck(@QueryParameter final String value) {
                if (isValidPort(value)) {
                        return FormValidation.ok();
                } else {
                        return FormValidation.error("please set valid port number. instead of" + value);
                }
        }
        
        private String nullify(String v) {
            if(v!=null && v.length()==0)    v=null;
            return v;
        }

        private int nullify(String v, int defaultValue) {
        	String value = nullify(v);
        	if (value == null) {
        		return defaultValue;
        	} else {
        		return Integer.parseInt(value);
        	}
        }
        private boolean isValidPort(String value) {
        	if (StringUtils.isNotEmpty(value)) {
        		try {
        			int port = Integer.parseInt(value);
        			if (MINMUM_PORT_NUMBER <= port && port <= MAXMUM_PORT_NUMBER) {
        				return true;
        			}
        		} catch (NumberFormatException ne) {
        			// do nothing 
        		}
        	}
        	return false;
        }
    }
    @Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}
    

}