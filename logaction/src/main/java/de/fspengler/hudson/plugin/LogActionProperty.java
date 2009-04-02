package de.fspengler.hudson.plugin;

import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * This Property sets BuildLog Action. 
 * 
 * @author tspengler
 */
public class LogActionProperty extends JobProperty<Job<?, ?>> {
    
    public JobPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
    
    public static final LogActionDescriptor DESCRIPTOR = new LogActionDescriptor();

    public static final class LogActionDescriptor extends JobPropertyDescriptor {

        private Boolean enabled;
        
        private Boolean restartEnabled;
        
        private String restartPattern;
        
        private String restartDescription;
        
        private Integer restartDelay;
        
        private transient Pattern pattern = null;
        
		public LogActionDescriptor() {
            super(LogActionProperty.class);
            load();
        }
        
        @Override
        public String getDisplayName() {
            return "BuildLog Action";
        }
        

        @Override
        public LogActionProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return new LogActionProperty();
        }
        
        @Override
        public boolean configure(StaplerRequest req) throws FormException {
            enabled = req.getParameter("logaction.enabled") != null;
            restartEnabled = req.getParameter("logaction.restartEnabled") != null;
            restartPattern = req.getParameter("logaction.restartPattern");
            restartDescription = req.getParameter("logaction.restartDescription");
            restartDelay = Integer.decode(req.getParameter("logaction.restartDelay"));
            pattern=null;
            save();
            return true;
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

        public boolean isEnabled() {
            return (enabled != null) ? enabled : false;
        }

        public void setEnabled(Boolean enable) {
            this.enabled = enable;
        }
        
        public String getRestartPattern() {
			return this.restartPattern; 
		}

		public void setRestartPattern(String restartPattern) {
			this.restartPattern = restartPattern;
			pattern=null;
		}

		public boolean isRestartEnabled() {
			return (restartEnabled != null) ? restartEnabled : false; 
		}

		public void setRestartEnabled(Boolean restartEnabled) {
			this.restartEnabled = restartEnabled;
		}

		public Pattern getPatternForRestart(){
			if (pattern==null){
				if (this.restartPattern != null){
					pattern = Pattern.compile(this.restartPattern);
				}
			}
			return pattern;
		}

		public String getRestartDescription() {
			if (restartDescription == null){
				// default
				restartDescription = "restart on Pattern";
			}
			return restartDescription;
		}

		public void setRestartDescription(String restartDescription) {
			this.restartDescription = restartDescription;
		}

		public Integer getRestartDelay() {
			if (restartDelay == null){
				// default
				restartDelay = 10;
			}
			return restartDelay;
		}

		public void setRestartDelay(Integer restartDelay) {
			this.restartDelay = restartDelay;
		}

    }         
}

    

