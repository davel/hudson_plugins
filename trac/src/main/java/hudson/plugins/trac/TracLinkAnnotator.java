package hudson.plugins.trac;

import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.regex.Pattern;

/**
 * Annotates <a href="http://trac.edgewall.org/wiki/TracLinks">TracLink</a>
 * notation in changelog messages.
 *
 * @author Kohsuke Kawaguchi
 */
public class TracLinkAnnotator extends ChangeLogAnnotator {
    @Override
    public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
        for (LinkMarkup markup : MARKUPS)
            markup.process(text);
    }

    private static final class LinkMarkup {
        private final Pattern pattern;
        private final String href;

        LinkMarkup(String pattern, String href) {
            pattern = NUM_PATTERN.matcher(pattern).replaceAll("(\\\\d+)"); // \\\\d becomes \\d when in the expanded text.
            pattern = ANYWORD_PATTERN.matcher(pattern).replaceAll("((?:\\\\w|[._-])+)");
            this.pattern = Pattern.compile(pattern);
            this.href = href;
        }

        void process(MarkupText text) {
            String url = TracProjectProperty.DESCRIPTOR.tracWebsite;
            if(url==null)   return; // not configured yet

            for(SubText st : text.findTokens(pattern)) {
                st.surroundWith(
                    "<a href='"+url+href+"'>",
                    "</a>");
            }
        }

        private static final Pattern NUM_PATTERN = Pattern.compile("NUM");
        private static final Pattern ANYWORD_PATTERN = Pattern.compile("ANYWORD");
    }

    private static final LinkMarkup[] MARKUPS = new LinkMarkup[] {
        new LinkMarkup(
            "(?:#|ticket:)NUM",
            "ticket/$1"),
        new LinkMarkup(
            "comment:ticket:NUM:NUM",
            "ticket/$1#comment:$2"),
        new LinkMarkup(
            "\\{NUM\\}|report:NUM",
            "report/$1$2"),  // only $1 or $2 matches, and the other will expand to ""
        new LinkMarkup(
            "rNUM:NUM|\\[NUM:NUM\\]|log:@NUM:NUM",
            "log/?rev=$2$4$6&stop_ver=$1$3$5"),
        new LinkMarkup(
            "rNUM(?!:)|\\[NUM\\]|changeset:NUM", // (?!:) is a position match with negative look ahead, so that "r5" portion of "r5:6" won't match.
            "changeset/$1$2$3"),
        // TODO: log:trunk@1:3 format
        // TODO: diffs
        new LinkMarkup(
            "((?:[A-Z][a-z]+)+)|wiki:ANYWORD",
            "wiki/$1$2"),
        new LinkMarkup(
            "milestone:ANYWORD",
            "milestone/$1"),
        // TODO: attachment and file.
    };
}
