package hudson.plugins.codeplex.browsers;

import hudson.model.Descriptor;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.codeplex.PluginImpl;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import hudson.scm.SubversionRepositoryBrowser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * {@link SubversionRepositoryBrowser} that produces CodePlex links.
 * 
 * @author Erik Ramfelt
 */
public class CodePlexSubversionBrowser extends SubversionRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public CodePlexSubversionBrowser() {
    }
    
    /**
     * Returns the code plex project property for the project behind the log entry
     * @param logentry entry to get project from
     * @return the code plex project property, or null.
     */
    CodePlexProjectProperty getProperty(LogEntry logentry) {
        return logentry.getParent().build.getProject().getProperty(CodePlexProjectProperty.class);
    }

    /**
     * Gets a URL for the {@link CodePlexProjectProperty#projectName} value
     * configured for the current project.
     */
    private URL getCodePlexWebURL(LogEntry cs) throws MalformedURLException {
        CodePlexProjectProperty property = getProperty(cs);
        if ((property == null) || (property.projectName == null))
            return null;
        else
            return new URL(property.getProjectUrlString());
    }

    /**
     * http://www.codeplex.com/SvnBridge/SourceControl/DirectoryView.aspx?SourcePath=&changeSetId=18677
     */
    @Override
    public URL getDiffLink(Path path) throws IOException {
//        if(path.getEditType()!= EditType.EDIT)
//            return null;    // no diff if this is not an edit change
//        URL baseUrl = getCodePlexWebURL(path.getLogEntry());
//        int revision = path.getLogEntry().getRevision();
//        return new URL(baseUrl, "source/diff?r=" + revision + "&format=side&path=" + path.getValue());
        return null;
    }

    /**
     * http://www.codeplex.com/SvnBridge/SourceControl/FileView.aspx?itemId=126383&changeSetId=18677
     */
    @Override
    public URL getFileLink(Path path) throws IOException {
//        URL baseUrl = getCodePlexWebURL(path.getLogEntry());
//        int revision = path.getLogEntry().getRevision();
//        return baseUrl == null ? null : new URL(baseUrl, "source/browse" + path.getValue() + "?r=" + revision + "#1");
        return null;
    }

    /**
     * http://www.codeplex.com/SvnBridge/SourceControl/DirectoryView.aspx?SourcePath=&changeSetId=18677
     */
    @Override
    public URL getChangeSetLink(LogEntry changeSet) throws IOException {
        URL baseUrl = getCodePlexWebURL(changeSet);
        return baseUrl == null ? null : new URL(baseUrl, "SourceControl/DirectoryView.aspx?SourcePath=&changeSetId=" + changeSet.getRevision());
    }

    public DescriptorImpl getDescriptor() {
        return PluginImpl.REPOSITORY_BROWSER_DESCRIPTOR;
    }

    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(CodePlexSubversionBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return "CodePlex";
        }
    }
}
