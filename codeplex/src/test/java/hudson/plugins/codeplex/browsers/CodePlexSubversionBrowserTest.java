package hudson.plugins.codeplex.browsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.scm.SubversionChangeLogSetFactory;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class CodePlexSubversionBrowserTest {

    @Test
    public void assertDiffLinkReturnsNull() throws Throwable {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);

        LogEntry entry = new LogEntry();
        Path path = new Path();
        path.setLogEntry(entry);      
        SubversionChangeLogSetFactory.setLogEntryParent(build, new LogEntry[]{entry});

        URL actual = new CodePlexSubversionBrowser().getDiffLink(path);
        assertNull(actual);
    }

    @Test
    public void assertFileLinkReturnsNull() throws Throwable {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);

        LogEntry entry = new LogEntry();
        entry.setRevision(446);
        Path path = new Path();
        path.setValue("/trunk/src/org/mockito/ArgumentMatcher.java");
        path.setAction("EDIT");      
        SubversionChangeLogSetFactory.setLogEntryParent(build, new LogEntry[]{entry});

        URL actual = new CodePlexSubversionBrowser().getFileLink(path);
        assertNull(actual);
    }

    @Test
    public void testGetChangeSetLinkLogEntry() throws Throwable {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("project"));
        stub(build.getProject()).toReturn(project);

        LogEntry entry = new LogEntry();
        entry.setRevision(450);      
        SubversionChangeLogSetFactory.setLogEntryParent(build, new LogEntry[]{entry});

        URL actual = new CodePlexSubversionBrowser().getChangeSetLink(entry);
        URL expected = new URL("http://www.codeplex.com/project/SourceControl/DirectoryView.aspx?SourcePath=&changeSetId=450");
        assertEquals(expected.toString(), actual.toString());
    }    
}
