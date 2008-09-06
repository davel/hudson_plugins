package hudson.plugins.codeplex.browsers;

import java.io.IOException;
import java.net.URL;

import hudson.model.Descriptor;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.codeplex.PluginImpl;
import hudson.plugins.tfs.browsers.TeamFoundationServerRepositoryBrowser;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.ChangeSet.Item;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * TFS Browser for Codeplex projects.
 * 
 * @author Erik Ramfelt
 */
public class CodePlexTfsBrowser extends TeamFoundationServerRepositoryBrowser {

    private static final long serialVersionUID = 1L;
    
    @DataBoundConstructor
    public CodePlexTfsBrowser() {
    }
    
    CodePlexProjectProperty getProperty(ChangeSet entry) {
        return entry.getParent().build.getProject().getProperty(CodePlexProjectProperty.class);
    }
    
    CodePlexProjectProperty getProperty(LogEntry entry) {
        return entry.getParent().build.getProject().getProperty(CodePlexProjectProperty.class);
    }

    @Override
    public URL getChangeSetLink(ChangeSet changeSet) throws IOException {        
        CodePlexProjectProperty property = changeSet.getParent().build.getProject().getProperty(CodePlexProjectProperty.class);
        if (property != null) {
            return new URL(property.getProjectUrlString() + "SourceControl/DirectoryView.aspx?SourcePath=&changeSetId=" + changeSet.getVersion());
        } else {
            return null;
        }
    }

    @Override
    public URL getDiffLink(Item item) throws IOException {
        return null;
    }

    @Override
    public URL getFileLink(Item item) throws IOException {
        return null;
    }
    
    public Descriptor<RepositoryBrowser<?>> getDescriptor() {
        return PluginImpl.TFS_BROWSER_DESCRIPTOR;
    }

    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(CodePlexTfsBrowser.class);
        }
        
        @Override
        public String getDisplayName() {
            return "CodePlex";
        }
    }
}
