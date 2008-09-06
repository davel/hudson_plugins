package hudson.plugins.codeplex.scm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.codeplex.CodePlexUserProperty;
import hudson.plugins.tfs.ChangeSetReader;
import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;

import org.junit.Test;

public class TfsChangeLogParserDecoratorTest {
    @Test public void assertTfsUsernameIsDecorated() throws Exception {
        List<ChangeSet> changesetList = new ArrayList<ChangeSet>();
        changesetList.add(new FakeChangeSet("1", new Date(), "snd\\redsolo_cp", "", mock(User.class)));
        ChangeLogSet changeLogSet = new ChangeLogSet(mock(AbstractBuild.class), changesetList);
        
        ChangeSetReader decoratedLogParser = mock(ChangeSetReader.class);
        stub(decoratedLogParser.parse(null, (File) null)).toReturn(changeLogSet);
        
        TfsChangeLogParserDecorator logParser = new TfsChangeLogParserDecorator(decoratedLogParser);
        ChangeLogSet logSet = (ChangeLogSet) logParser.parse(null, null);
        assertEquals("The user name was not fixed", "redsolo", logSet.iterator().next().getUser());
    }
    
    @Test public void assertTfsUsernameWithoutDomainIsNotDecorated() throws Exception {
        List<ChangeSet> changesetList = new ArrayList<ChangeSet>();
        changesetList.add(new FakeChangeSet("1", new Date(), "redsolo", "", mock(User.class)));
        ChangeLogSet changeLogSet = new ChangeLogSet(mock(AbstractBuild.class), changesetList);
        
        ChangeSetReader decoratedLogParser = mock(ChangeSetReader.class);
        stub(decoratedLogParser.parse(null, (File) null)).toReturn(changeLogSet);
        
        TfsChangeLogParserDecorator logParser = new TfsChangeLogParserDecorator(decoratedLogParser);
        ChangeLogSet logSet = (ChangeLogSet) logParser.parse(null, null);
        assertEquals("The user name was not fixed", "redsolo", logSet.iterator().next().getUser());
    }
    
    @Test public void assertUserPropertyIsAddedToUser() throws Exception {
        User user = mock(User.class);
        stub(user.getProperty(CodePlexUserProperty.class)).
            toReturn(null).
            toReturn(new CodePlexUserProperty());
        
        List<ChangeSet> changesetList = new ArrayList<ChangeSet>();
        changesetList.add(new FakeChangeSet("1", new Date(), "redsolo", "", user));
        changesetList.add(new FakeChangeSet("2", new Date(), "redsolo", "", user));
        ChangeLogSet changeLogSet = new ChangeLogSet(mock(AbstractBuild.class), changesetList);
        
        ChangeSetReader decoratedLogParser = mock(ChangeSetReader.class);
        stub(decoratedLogParser.parse(null, (File) null)).toReturn(changeLogSet);
        
        TfsChangeLogParserDecorator logParser = new TfsChangeLogParserDecorator(decoratedLogParser);
        logParser.parse(null, null);
        
        verify(user).addProperty(isA(CodePlexUserProperty.class));
    }
    
    private static class FakeChangeSet extends ChangeSet {
        private final User user;

        public FakeChangeSet(String version, Date date, String userName, String comment, User user) {
            super(version, date, userName, comment);
            this.user = user;
        }

        @Override
        public User getAuthor() {
            return user;
        }
        
    }
}
