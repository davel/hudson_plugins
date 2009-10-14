/* Copyright (c) 2007, http://www.codeviation.org project 
 * This program is made available under the terms of the MIT License. 
 */

package hudson.plugins.codeviation;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.model.Project;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author pzajac
 */
public class CodeviationPublisher extends Recorder {

    public CodeviationPublisher() {
    }

    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException,IOException {
        return true;                                                            
    }

    @Override
    public Action getProjectAction(AbstractProject<?,?> prj) {
        return prj instanceof Project ? new MetricsAction((Project)prj) : null;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(CodeviationPublisher.class);
        }

        public String getDisplayName() {
            return "Codeviation publisher";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/codeviation/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return Project.class.isAssignableFrom(jobType);
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            CodeviationPublisher pub = new CodeviationPublisher();
          //  req.bindParameters(pub, "codeviationpublisher.");
            return pub;
        }
    }

}
