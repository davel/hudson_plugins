package edu.ucsc.soe.sep;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Created by IntelliJ IDEA.
 * User: cflewis
 * Date: Jan 9, 2010
 * Time: 5:38:11 PM
 */
@Extension
public class SepDescriptor extends BuildStepDescriptor<Publisher> {
    public SepDescriptor() {
        super(SepRecorder.class);
    }

    @Override
    public String getDisplayName() {
        return "Sep";
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData)
            throws hudson.model.Descriptor.FormException {
        return req.bindJSON(SepRecorder.class,  formData);
    }
}
