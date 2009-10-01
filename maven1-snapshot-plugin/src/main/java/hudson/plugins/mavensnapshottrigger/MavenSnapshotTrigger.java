package hudson.plugins.mavensnapshottrigger;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AbstractBuild;
import static hudson.Util.fixNull;
import hudson.model.BuildableItem;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.scheduler.CronTabList;
import hudson.util.FormValidation;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.net.URL;

import hudson.model.Project;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.File;


/**
 * {@link Trigger} that detects changes in Maven 1.x SNAPSHOT dependencies.
 *
 * @author Jarkko Viinamäki 
 */
public class MavenSnapshotTrigger extends Trigger<BuildableItem> {
    
    private static final Logger LOGGER = Logger.getLogger(MavenSnapshotTrigger.class.getName());
    
    public MavenSnapshotTrigger(String cronTabSpec) throws ANTLRException {
        super(cronTabSpec);
    }

    @Override
    public void run() {
        try {
            Project project = (Project)job;
        
            AbstractBuild build = (AbstractBuild)project.getLastBuild();
            
            LOGGER.fine("Polling SNAPSHOT changes for "+project.getName());
            
            if( build != null ) {
                MavenSnapshotScanner scanner = new MavenSnapshotScanner();

                // We assume that the project.xml is in the project root
                URL url = new URL(build.getModuleRoot().toURI() + "/project.xml");
                
                File projectXml = new File(url.getFile());
                
                scanner.setProjectFile(projectXml.getAbsolutePath());

                Date lastBuild = new Date(build.getTimestamp().getTimeInMillis());

                List<File> modifications = scanner.getModifications(lastBuild);
                
                if( modifications.size() > 0 ) {
                    LOGGER.info(project.getName()+": "+modifications.size() + " modifications found. Triggering a build.");

                    // TODO: how to report dependency changes to Hudson (change set)?
                    
                    // trigger a build
                    job.scheduleBuild(new MavenSnapshotCause(modifications));
                }
            }
        }
        catch(Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public TriggerDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final TriggerDescriptor DESCRIPTOR = new DescriptorImpl();
        
    public static class DescriptorImpl extends TriggerDescriptor {
        
        // private class attributes are automatically persisted
        
        public DescriptorImpl() {
            super(MavenSnapshotTrigger.class);
            load(); // load private attribute values from disk
        }
        
        public boolean isApplicable(Item item) {
            return item instanceof BuildableItem;
        }
        
        public String getDisplayName() {
            return "Maven 1.x SNAPSHOT dependency trigger";
        }
        
        /**
         * Returns the resource path to the help screen HTML, if any.
         *
         * <p>
         * This value is relative to the context root of Hudson, so normally
         * the values are something like <tt>"/plugin/emma/help.html"</tt> to
         * refer to static resource files in a plugin, or <tt>"/publisher/EmmaPublisher/abc"</tt>
         * to refer to Jelly script <tt>abc.jelly</tt> or a method <tt>EmmaPublisher.doAbc()</tt>.
         *
         * @return
         *      "" to indicate that there's no help.
         */
        @Override
        public String getHelpFile() {
            return "/plugin/maven-snapshot-plugin/help-projectConfig.html";
        }
        
        /**
         * Invoked when the global configuration page is submitted.
         *
         * Can be overriden to store descriptor-specific information.
         *
         * @return false
         *      to keep the client in the same config page.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            // myParam = req.getParameter("mavensnapshotplugin.myParam")!=null;
            save();
            return super.configure(req, formData);
        }
        
        /**
         * Performs syntax check.
         */
        public FormValidation doCheck(@QueryParameter final String mavensnapshotplugin_spec) {
            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return FormValidation.ok();
            try {
                CronTabList.create(fixNull(mavensnapshotplugin_spec));
                return FormValidation.ok();
            } catch (ANTLRException e) {
                return FormValidation.error(e.getMessage());
            }
        }        

        /**
         * Creates a configured instance from the submitted form.
         * <p>
         * Hudson only invokes this method when the user wants an instance of <tt>T</tt>.
         * So there's no need to check that in the implementation.
         *
         * @param req
         *      Always non-null. This object includes all the submitted form values.
         *
         * @throws FormException
         *      Signals a problem in the submitted form.
         */
        @Override
        public Trigger newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            try {
                return new MavenSnapshotTrigger(req.getParameter("mavensnapshotplugin_spec"));
            } catch (ANTLRException e) {
                throw new FormException(e.toString(), e, "mavensnapshotplugin_spec");
            }
        }
    };
}