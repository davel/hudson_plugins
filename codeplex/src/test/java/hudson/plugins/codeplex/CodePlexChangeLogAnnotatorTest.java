package hudson.plugins.codeplex;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import hudson.MarkupText;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.codeplex.CodePlexChangeLogAnnotator;
import hudson.plugins.codeplex.CodePlexProjectProperty;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class CodePlexChangeLogAnnotatorTest {

    @Test
    public void testNoWikiLinkToAnnotate() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);
        
        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
        MarkupText markupText = new MarkupText("Ordinary commit message without wiki link.");
        annotator.annotate(build, null, markupText);
        assertEquals("Ordinary commit message without wiki link.", markupText.toString() );
    }

//    @Test
//    public void assertWikiWordIsAnnotated() {
//        AbstractBuild build = mock(AbstractBuild.class);
//        AbstractProject<?, ?> project = mock(AbstractProject.class);
//        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
//        stub(build.getProject()).toReturn(project);
//
//        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
//        MarkupText markupText = new MarkupText("Message with WikiLink. Yes a link.");
//        annotator.annotate(build, null, markupText);
//        assertEquals("Message with <a href='http://www.codeplex.com/theproject/Wiki/View.aspx?title=WikiLink'>WikiLink</a>. Yes a link.", markupText.toString() );
//    }

    @Test
    public void assertWikiWordInBracketsIsAnnotated() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);

        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
        MarkupText markupText = new MarkupText("Message with [Wiki Link]. Yes a link.");
        annotator.annotate(build, null, markupText);
        assertEquals("Message with <a href='http://www.codeplex.com/theproject/Wiki/View.aspx?title=Wiki Link'>[Wiki Link]</a>. Yes a link.", markupText.toString() );
    }

    @Test
    public void assertWikiKeyWordIsAnnotated() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);

        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
        MarkupText markupText = new MarkupText("Message with wiki:WikiLink. Yes a link.");
        annotator.annotate(build, null, markupText);
        assertEquals("Message with <a href='http://www.codeplex.com/theproject/Wiki/View.aspx?title=WikiLink'>wiki:WikiLink</a>. Yes a link.", markupText.toString() );
    }

//    @Test
//    public void assertOneLetterWikiWordIsAnnotated() {
//        AbstractBuild build = mock(AbstractBuild.class);
//        AbstractProject<?, ?> project = mock(AbstractProject.class);
//        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
//        stub(build.getProject()).toReturn(project);
//
//        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
//        MarkupText markupText = new MarkupText("Changed version ThisIsAWikiLink");
//        annotator.annotate(build, null, markupText);
//        assertEquals("Changed version <a href='http://www.codeplex.com/theproject/Wiki/View.aspx?title=ThisIsAWikiLink'>ThisIsAWikiLink</a>", markupText.toString() );
//    }

    @Test
    public void assertWorkItemInBracketsIsAnnotated() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);

        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
        MarkupText markupText = new MarkupText("Message with [workitem: 12]. Yes a link.");
        annotator.annotate(build, null, markupText);
        assertEquals("Message with <a href='http://www.codeplex.com/theproject/WorkItem/View.aspx?WorkItemId=12'>[workitem: 12]</a>. Yes a link.", markupText.toString() );
    }

    @Test
    public void assertDiscussionInBracketsIsAnnotated() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getProperty(CodePlexProjectProperty.class)).toReturn(new CodePlexProjectProperty("theproject"));
        stub(build.getProject()).toReturn(project);

        CodePlexChangeLogAnnotator annotator = new CodePlexChangeLogAnnotator();
        MarkupText markupText = new MarkupText("Message with [discussion: 12]. Yes a link.");
        annotator.annotate(build, null, markupText);
        assertEquals("Message with <a href='http://www.codeplex.com/theproject/Thread/View.aspx?ThreadId=12'>[discussion: 12]</a>. Yes a link.", markupText.toString() );
    }
}
