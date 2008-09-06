package hudson.plugins.codeplex;

import hudson.Plugin;
import hudson.model.Jobs;
import hudson.model.UserProperties;
import hudson.plugins.codeplex.browsers.CodePlexSubversionBrowser;
import hudson.plugins.codeplex.browsers.CodePlexTfsBrowser;
import hudson.plugins.codeplex.scm.CodePlexTfsScm;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCMS;

/**
 * Entry point of the plugin.
 *
 * @author Erik Ramfelt
 * @plugin
 */
public class PluginImpl extends Plugin {
    
    private final CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();

    public static final CodePlexProjectProperty.DescriptorImpl PROJECT_PROPERTY_DESCRIPTOR = new CodePlexProjectProperty.DescriptorImpl();
    public static final CodePlexUserProperty.DescriptorImpl USER_PROPERTY_DESCRIPTOR = new CodePlexUserProperty.DescriptorImpl(); 

    public static final CodePlexSubversionBrowser.DescriptorImpl REPOSITORY_BROWSER_DESCRIPTOR = new CodePlexSubversionBrowser.DescriptorImpl();
    public static final CodePlexTfsBrowser.DescriptorImpl TFS_BROWSER_DESCRIPTOR = new CodePlexTfsBrowser.DescriptorImpl();
    public static final CodePlexTfsScm.DescriptorImpl TFS_SCM_DESCRIPTOR = new CodePlexTfsScm.DescriptorImpl();

    
    @Override
    public void start() throws Exception {
        annotator.register();
        Jobs.PROPERTIES.add(PROJECT_PROPERTY_DESCRIPTOR);
        RepositoryBrowsers.LIST.add(REPOSITORY_BROWSER_DESCRIPTOR);
        SCMS.SCMS.add(TFS_SCM_DESCRIPTOR);
        UserProperties.LIST.add(USER_PROPERTY_DESCRIPTOR);
    }

    @Override
    public void stop() throws Exception {
        UserProperties.LIST.remove(USER_PROPERTY_DESCRIPTOR);
        SCMS.SCMS.remove(TFS_SCM_DESCRIPTOR);
        RepositoryBrowsers.LIST.remove(REPOSITORY_BROWSER_DESCRIPTOR);
        Jobs.PROPERTIES.remove(PROJECT_PROPERTY_DESCRIPTOR);
        annotator.unregister();
    }
}
