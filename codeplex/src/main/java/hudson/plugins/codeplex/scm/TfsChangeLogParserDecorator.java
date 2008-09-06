package hudson.plugins.codeplex.scm;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import hudson.model.AbstractBuild;
import hudson.plugins.codeplex.CodePlexUserProperty;
import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet.Entry;

/**
 * Strips away the domain and <tt>_cp</tt> from the codeplex user names.
 * It will also add a code plex user property to the user.
 * 
 * @author Erik Ramfelt
 */
public class TfsChangeLogParserDecorator extends ChangeLogParser {

    private final ChangeLogParser decoratedChangeLogParser;

    public TfsChangeLogParserDecorator(ChangeLogParser other) {
        this.decoratedChangeLogParser = other;
    }
    
    @Override
    public hudson.scm.ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        ChangeLogSet logSet = (ChangeLogSet) decoratedChangeLogParser.parse(build, changelogFile);
        for (ChangeSet changeset : logSet) {
            changeset.setUser(removeDomainAndSuffix(changeset.getUser()));
            if (changeset.getAuthor() != null) {
                if (changeset.getAuthor().getProperty(CodePlexUserProperty.class) == null) {
                    changeset.getAuthor().addProperty(new CodePlexUserProperty());
                }
            }
        }
        return logSet;
    }

    private String removeDomainAndSuffix(String user) {
        return user.replaceAll("_cp$", "");
    }
}
