package hudson.plugins.mavensnapshottrigger;

import antlr.ANTLRException;
import static hudson.Util.fixNull;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.scheduler.CronTabList;
import hudson.util.FormFieldValidator;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import java.net.URL;

import hudson.model.Run;
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

    public void run() {
        try {
            Project project = (Project)job;
        
            Run build = project.getLastBuild();
            
            LOGGER.fine("Polling SNAPSHOT changes for "+project.getName());
            
            if( build != null ) {
                MavenSnapshotScanner scanner = new MavenSnapshotScanner();

                // We assume that the project.xml is in the project root
                URL url = new URL(project.getModuleRoot().toURI() + "/project.xml");
                
                File projectXml = new File(url.getFile());
                
                scanner.setProjectFile(projectXml.getAbsolutePath());

                Date lastBuild = new Date(build.getTimestamp().getTimeInMillis());

                List modifications = scanner.getModifications(lastBuild);
                
                if( modifications.size() > 0 ) {
                    LOGGER.info(project.getName()+": "+modifications.size() + " modifications found. Triggering a build.");

                    // TODO: how to report dependency changes to Hudson (change set)?
                    
                    // trigger a build
                    job.scheduleBuild();
                }
            }
        }
        catch(Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public TriggerDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

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
        public boolean configure(StaplerRequest req) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            // myParam = req.getParameter("mavensnapshotplugin.myParam")!=null;
            save();
            return super.configure(req);
        }
        
        /**
         * Performs syntax check.
         */
        public void doCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator(req, rsp, true) {
                protected void check() throws IOException, ServletException {
                    try {
                        CronTabList.create(fixNull(request.getParameter("mavensnapshotplugin_spec")));
                        ok();
                    } catch (ANTLRException e) {
                        error(e.getMessage());
                    }
                }
            }.process();
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
        public Trigger newInstance(StaplerRequest req) throws FormException {
            try {
                return new MavenSnapshotTrigger(req.getParameter("mavensnapshotplugin_spec"));
            } catch (ANTLRException e) {
                throw new FormException(e.toString(), e, "mavensnapshotplugin_spec");
            }
        }
    };
}