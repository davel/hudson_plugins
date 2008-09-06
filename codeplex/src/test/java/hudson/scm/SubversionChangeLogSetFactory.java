package hudson.scm;

import hudson.model.AbstractBuild;
import hudson.scm.SubversionChangeLogSet.LogEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * A very ugly hack to be able to fake a subversion log set in a test.
 * The SubversionChangeLogSet class has one constructor which is package
 * protected which makes it impossible in testing to create one. The class
 * is also final which makes it impossible to mock. Therefore I could not
 * find any other way than create a class in the same package to go around
 * the problem.
 */
@SuppressWarnings("unchecked")
public class SubversionChangeLogSetFactory {

    public static SubversionChangeLogSet create(AbstractBuild build) {
        return create(build, new ArrayList<LogEntry>());
    }
    
    public static SubversionChangeLogSet create(AbstractBuild build, LogEntry[] logs) {
        ArrayList<LogEntry> list = new ArrayList<LogEntry>(logs.length);
        for (LogEntry entry : logs) {
            list.add(entry);
        }
        return create(build, list);
    }
    
    public static SubversionChangeLogSet create(AbstractBuild build, List<LogEntry> logs) {
        return new SubversionChangeLogSet(build, logs);
    }
    
    public static void setLogEntryParent(AbstractBuild build, LogEntry[] logs) {
        create(build, logs);
    }
}
