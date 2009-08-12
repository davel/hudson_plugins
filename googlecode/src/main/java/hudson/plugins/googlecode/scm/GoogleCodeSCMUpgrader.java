package hudson.plugins.googlecode.scm;

import java.util.List;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import hudson.model.listeners.ItemListener;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.scm.SCM;

/**
 * ItemListener that will upgrade GoogleCodeSCM objects to GoogleCodeSCMEx objects.
 * Due to the bug https://hudson.dev.java.net/issues/show_bug.cgi?id=4136, the old Google code SCM
 * objects has to be reconfigured to use the new GoogleCodeSCMEx.
 * 
 * @author redsolo
 */
@Extension
public class GoogleCodeSCMUpgrader extends ItemListener {
    private static final Logger logger = Logger.getLogger(GoogleCodeSCMUpgrader.class.getName());
    
    @Override
    public void onLoaded() {        
        onLoaded(Hudson.getInstance().getItems());
    }

    /**
     * Goes through the items to upgrade each GoogleCodeSCM to GoogleCodeSCMEx
     * Method extracted because it is easier to mock TopLevelItems than start a Hudson test case
     * @param items list of top level items
     */
    void onLoaded(List<TopLevelItem> items) {
        for (TopLevelItem item : items) {
            if (item instanceof AbstractProject<?,?>) {
                AbstractProject<?, ?> abstractProject = (AbstractProject<?,?>) item;
                SCM oldScm = abstractProject.getScm();
                if (oldScm instanceof GoogleCodeSCM) {                    
                    upgradeScmInProject(abstractProject, (GoogleCodeSCM) oldScm);
                }
            }
        }
    }

    /**
     * Upgrades the SCM in project with data from the old scm
     * @param abstractProject project containing a GoogleCodeProjectProperty
     * @param oldScm old scm containing remote directory
     */
    void upgradeScmInProject(AbstractProject<?, ?> abstractProject, GoogleCodeSCM oldScm) {
        String name = abstractProject.getName();
        try {
            GoogleCodeSCMEx scmCopy = copy(oldScm, abstractProject.getProperty(GoogleCodeProjectProperty.class));
            abstractProject.setScm(scmCopy);
            abstractProject.save();
            logger.info("Upgraded Google Code SCM in '" + name + "'.");
        } catch (Exception e) {
            logger.warning("Error while upgrading Google Code SCM configuration for '" + name + 
                    "'. The project is still usable, but should be manually configured again. " +
                    "For more information please see https://hudson.dev.java.net/issues/show_bug.cgi?id=4136.");
            abstractProject.setScm(oldScm);
        }
    }

    /**
     * Copies the old GoogleCodeSCM to a new GoogleCodeSCMEx object
     * @param source scm to copy
     * @param property property containing the project URL
     * @return a new GoogleCodeSCMEx class
     */
    GoogleCodeSCMEx copy(GoogleCodeSCM source, GoogleCodeProjectProperty property) {
        return GoogleCodeSCMEx.DescriptorImpl.newInstance(property, source.getDirectory());
    }
}
