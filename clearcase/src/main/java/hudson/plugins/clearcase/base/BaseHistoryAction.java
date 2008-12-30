package hudson.plugins.clearcase.base;

import static hudson.plugins.clearcase.util.OutputFormat.*;
import hudson.plugins.clearcase.ClearCaseChangeLogEntry;
import hudson.plugins.clearcase.ClearTool;
import hudson.plugins.clearcase.history.AbstractHistoryAction;
import hudson.plugins.clearcase.history.Filter;
import hudson.plugins.clearcase.history.HistoryEntry;
import hudson.plugins.clearcase.util.ChangeLogEntryMerger;
import hudson.plugins.clearcase.util.ClearToolFormatHandler;

import hudson.scm.ChangeLogSet.Entry;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 *
 * @author hlyh
 */
public class BaseHistoryAction extends AbstractHistoryAction{

    private static final String[] HISTORY_FORMAT = {DATE_NUMERIC,
        USER_ID,        
        NAME_ELEMENTNAME,
        NAME_VERSIONID,
        EVENT,
        OPERATION
    };

    private ClearToolFormatHandler historyHandler = new ClearToolFormatHandler(HISTORY_FORMAT);
    private int maxTimeDifferenceMillis;

    public BaseHistoryAction(ClearTool cleartool, List<Filter> filters,int maxTimeDifferenceMillis) {
        super(cleartool, filters);
        this.maxTimeDifferenceMillis = maxTimeDifferenceMillis;
    }

    @Override
    protected List<? extends Entry> buildChangelog(List<HistoryEntry> entries) {
        List<ClearCaseChangeLogEntry> fullList = new ArrayList<ClearCaseChangeLogEntry>();

        for (HistoryEntry entry : entries) {
                ClearCaseChangeLogEntry changelogEntry = new ClearCaseChangeLogEntry();

                changelogEntry.setDate(entry.getDate());
                changelogEntry.setUser(entry.getUser());
                changelogEntry.setComment(entry.getComment());

                ClearCaseChangeLogEntry.FileElement fileElement = new ClearCaseChangeLogEntry.FileElement(
                        entry.getElement(), entry.getVersionId(), entry.getEvent(), entry.getOperation());

                changelogEntry.addElement(fileElement);
            
        }
        ChangeLogEntryMerger entryMerger = new ChangeLogEntryMerger(maxTimeDifferenceMillis);
        return entryMerger.getMergedList(fullList);
    }

    @Override
    protected ClearToolFormatHandler getHistoryFormatHandler() {
        return historyHandler;
    }

    @Override
    protected HistoryEntry parseEventLine(Matcher matcher, String line) throws ParseException{
        // read values;
        HistoryEntry entry = new HistoryEntry();
        entry.setLine(line);

        entry.setDateText(matcher.group(1));
        entry.setUser(matcher.group(2).trim());
        entry.setElement(matcher.group(3).trim());
        entry.setVersionId(matcher.group(4).trim());
        entry.setEvent(matcher.group(5).trim());
        entry.setOperation(matcher.group(6).trim());
        return entry;
    }
}
