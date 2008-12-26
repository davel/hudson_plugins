package hudson.plugins.clearcase.history;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Henrik L. Hansen (henrik.lynggaard@gmail.com)
 */
public class HistoryEntry {

    Date date;
    String dateText;
    String element;
    String versionId;
    String event;
    String user;
    String operation;
    String activityName;
    String activityHeadline;

    public String getActivityHeadline() {
        return activityHeadline;
    }

    public void setActivityHeadline(String activityHeadline) {
        this.activityHeadline = activityHeadline;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    String line;

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss");
        date = format.parse(dateText);
        this.dateText = dateText;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Date getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HistoryEntry other = (HistoryEntry) obj;
        if ((this.line == null) ? (other.line != null) : !this.line.equals(other.line)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.line != null ? this.line.hashCode() : 0);
        return hash;
    }

}
