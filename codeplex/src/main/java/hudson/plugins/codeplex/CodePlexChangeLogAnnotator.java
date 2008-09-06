package hudson.plugins.codeplex;

import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.regex.Pattern;

/**
 * Annotates <a href="http://www.codeplex.com/CodePlex/Wiki/View.aspx?title=CodePlex%20Wiki%20Markup%20Guide">CodePlex Wiki Markup Guide</a>
 * notation in changelog messages. 
 *
 * @author Erik Ramfelt
 */
public class CodePlexChangeLogAnnotator extends ChangeLogAnnotator {


    /**
     * Return the property from the project for the build
     * @param build build to return property for
     * @return project property if it exists
     */
    private CodePlexProjectProperty getProperty(AbstractBuild<?,?> build) {
        return build.getProject().getProperty(CodePlexProjectProperty.class);
    }

    @Override
    public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
        CodePlexProjectProperty property = getProperty(build);
        if(property==null || property.projectName==null)
            return; // not configured

        String url = property.getProjectUrlString();
        for (LinkMarkup markup : MARKUPS) {
            markup.process(text, url);
        }
    }

    static final class LinkMarkup {
        private final Pattern pattern;
        private final String href;
        
        LinkMarkup(String pattern, String href) {
            this(pattern, href, 0);
        }

        LinkMarkup(String pattern, String href, int flags) {
            pattern = NUM_PATTERN.matcher(pattern).replaceAll("(\\\\d+)"); // \\\\d becomes \\d when in the expanded text.
            pattern = ANYWORD_PATTERN.matcher(pattern).replaceAll("((?:\\\\w|[_-])+)");
            this.pattern = Pattern.compile(pattern, flags);
            this.href = href;
        }

        void process(MarkupText text, String url) {
            for(SubText st : text.findTokens(pattern)) {
                st.surroundWith(
                    "<a href='"+url+href+"'>",
                    "</a>");
            }
        }

        private static final Pattern NUM_PATTERN = Pattern.compile("NUM");
        private static final Pattern ANYWORD_PATTERN = Pattern.compile("ANYWORD");
    }

    static final LinkMarkup[] MARKUPS = new LinkMarkup[] {
        new LinkMarkup(
                "\\[workitem:\\s*(NUM)]",
                "WorkItem/View.aspx?WorkItemId=$1",
                Pattern.CASE_INSENSITIVE),
        new LinkMarkup(
                "\\[discussion:\\s*(NUM)]",
                "Thread/View.aspx?ThreadId=$1",
                Pattern.CASE_INSENSITIVE),
        new LinkMarkup(
                "\\[([^:]+)]",
                "Wiki/View.aspx?title=$1"),
        new LinkMarkup(
                "wiki:(ANYWORD)",
                "Wiki/View.aspx?title=$1"),
    };
}
