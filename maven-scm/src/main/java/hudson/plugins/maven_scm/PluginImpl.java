package hudson.plugins.maven_scm;

import hudson.Plugin;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.manager.BasicScmManager;

/**
 * @plugin
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {

        //SCMS.SCMS.add(new ProviderSpecificDescriptor("ClearCase","clearcase"));
        //SCMS.SCMS.add(new ProviderSpecificDescriptor("Mercurial","hg"));
        //SCMS.SCMS.add(new ProviderSpecificDescriptor("CM Synergy","synergy"));
        //SCMS.SCMS.add(new ProviderSpecificDescriptor("Perforce","perforce"));
        //SCMS.SCMS.add(new ProviderSpecificDescriptor("StarTeam","starteam"));
        //SCMS.SCMS.add(new ProviderSpecificDescriptor("Visual SourceSafe","vss"));

    public static final ScmManager MANAGER = new BasicScmManager();
}
