package hudson.plugins.codeplex.browsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;

import java.net.URL;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class CodePlexTfsBrowserTest {
    
    @Test public void assertChangeSetLink() throws Exception {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("project"));
        stub(build.getProject()).toReturn(project);
        
        ChangeSet changeset = new ChangeSet("450", null, "user", "comment");
        new ChangeLogSet(build, new ChangeSet[]{changeset});
        URL actual = new CodePlexTfsBrowser().getChangeSetLink(changeset);
        assertEquals("The change set link was incorrect", "http://www.codeplex.com/project/SourceControl/DirectoryView.aspx?SourcePath=&changeSetId=450", actual.toString());
    }

    @Test public void assertLinkIsNullIfNoProjectProperty() throws Exception {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(null);
        stub(build.getProject()).toReturn(project);
        
        ChangeSet changeset = new ChangeSet("450", null, "user", "comment");
        new ChangeLogSet(build, new ChangeSet[]{changeset});
        URL actual = new CodePlexTfsBrowser().getChangeSetLink(changeset);
        assertNull("The change set link was not null", actual);
    }
}
