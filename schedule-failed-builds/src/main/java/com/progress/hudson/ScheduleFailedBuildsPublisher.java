package com.progress.hudson;

import java.io.IOException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;



/**
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest req, JSONObject formData)} is invoked
 * and a new {@link ScheduleFailedBuildsPublisher} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
 * will be invoked. 
 *
 * @author Stefan Fritz <sfritz@progress.com>
 */
public class ScheduleFailedBuildsPublisher extends Notifier {

    private  String interval="0";
    private  String maxRetries="0";
    
    public ScheduleFailedBuildsPublisher(String interval, String maxRetries) {
      setInterval(interval);
      setMaxRetries(maxRetries);
      
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
        throws InterruptedException, IOException {     
      // If the build was successful, remove it from our build queue
      if (build.getResult() == Result.SUCCESS) {
        // no need to build it
        FailedBuildsQueue.remove(build);          
      }
      else{
        if(!interval.equals("0")){
          //enabled
          FailedBuildsQueue.add(build, getInterval(),getMaxRetries());
        }
      }

      return true;
    }
    
    @Override
    public boolean needsToRunAfterFinalized() {
      return true;
    }

    /**
     * Descriptor for {@link ScheduleFailedBuildsPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * 
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(ScheduleFailedBuildsPublisher.class);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Schedule failed builds (ScheduleFailedBuildsPublisher)";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }


        /**
         * Creates a new instance of {@link ScheduleFailedBuildsPublisher} from a submitted form.
         */
        @Override
        public ScheduleFailedBuildsPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String interval = formData.getString("interval");
            String maxRetries = formData.getString("maxRetries");
            
            if(interval.length()==0){
              // disabled
              interval="0";
            }
            
            if(maxRetries.length()==0 || maxRetries.equals("0")){
              //forever
              maxRetries="-1";
            }
            
            return new ScheduleFailedBuildsPublisher(interval,maxRetries);            
        }
    }

    public String getInterval() {
      return interval;
    }

    public String getMaxRetries() {
      return maxRetries;
    }

    public void setInterval(String interval) {
      this.interval = interval;
    }

    public void setMaxRetries(String maxRetries) {
      this.maxRetries = maxRetries;
    }

   
}
