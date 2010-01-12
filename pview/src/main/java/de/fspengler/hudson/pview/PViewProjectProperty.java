package de.fspengler.hudson.pview;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormValidation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class PViewProjectProperty extends JobProperty<AbstractProject<?,?>> {

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends JobPropertyDescriptor {
    	
    	private String regex;
		private String treeSplitChar;
    	
        public DescriptorImpl() {
            super(PViewProjectProperty.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
        	return false;
        }

        public String getDisplayName() {
        	return "Filtered View";
        }
        
        @Override
        public PViewProjectProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        	return new PViewProjectProperty();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
        	regex = req.getParameter("pView.regex");
        	treeSplitChar = req.getParameter("pView.treeSplitChar");
            save();
            return true;
        }

        
        public String getRegex() {
        	if(regex == null) return ".*";
        	return regex;
        }
        
        @Exported
        public String getTreeSplitChar() {
    		checkEmptyTreeChar();
    		return treeSplitChar;
    	}

    	public void setTreeSplitChar(String treeSplitChar) {
    		this.treeSplitChar = treeSplitChar;
    	}

    	private void checkEmptyTreeChar() {
    		if (this.treeSplitChar == null || this.treeSplitChar.length() == 0){
            	this.treeSplitChar = "-";
            }
    	}
        
        
        /**
         * Checks if the Bugzilla URL is accessible and exists.
         */
        public FormValidation doRegexCheck(@QueryParameter String value) {
            if(Util.fixEmpty(value)==null) {
                return FormValidation.error("No regex");
            }
            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error("Pattern cannot be compiled");
            }
        }

        
    }
}
