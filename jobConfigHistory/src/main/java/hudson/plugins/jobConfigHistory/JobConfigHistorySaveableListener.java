package hudson.plugins.jobConfigHistory;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Saveable;
import hudson.model.User;
import hudson.model.listeners.SaveableListener;
import hudson.util.TextFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

/**
 * @author Stefan Brausch
 */
@Extension
public final class JobConfigHistorySaveableListener extends SaveableListener {

    @Override
    public void onChange(Saveable o, XmlFile file) {
        try {
            createNewHistoryEntry((Item) o, CHANGED);
        } catch (Exception e) {
            Logger.getLogger("Config History Exception: " + e.getMessage());
        }
        super.onChange(o, file);
    }

    protected static final String CHANGED = "Changed";

    protected static final SimpleDateFormat ID_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss");

    public JobConfigHistorySaveableListener() {
        super();

    }

    protected File getConfigsDir(Item item) {
        return new File(item.getRootDir(), "config-history");
    }

    protected File getRootDir(Item item, Calendar timestamp) {
        File f = new File(getConfigsDir(item), ID_FORMATTER.format(timestamp
                .getTime()));
        f.mkdirs();
        return f;
    }

    protected String createNewHistoryEntry(Item item, String operation) {
        Calendar timestamp = new GregorianCalendar();
        File myDir = getRootDir(item, timestamp);
        TextFile myConfig = new TextFile(new File(myDir, "config.xml"));

        String configContent = "";
        try {
            if (((AbstractProject<?, ?>) item).getConfigFile().exists())
                configContent = ((AbstractProject<?, ?>) item).getConfigFile()
                        .asString();
            myConfig.write(configContent);

            XmlFile myDescription = new XmlFile(new File(myDir, "history.xml"));

            String user = "Anonym";
            String userId = "";
            if (User.current() != null) {
                user = User.current().getFullName();
                userId = User.current().getId();
            }

            HistoryDescr myDescr = new HistoryDescr(user, userId, operation,
                    ID_FORMATTER.format(timestamp.getTime()));

            myDescription.write(myDescr);

        } catch (Exception e) {
            Logger.getLogger("Config History Exception: " + e.getMessage());
        }

        return myDir.getAbsolutePath();

    }

    protected void updateCommitData(String historyDir, String user, String descr)
            throws Exception {

        XmlFile myDescription = new XmlFile(new File(historyDir, "history.xml"));
        HistoryDescr oldDescr = (HistoryDescr) myDescription.read();
        HistoryDescr newDescr = new HistoryDescr(user, descr, oldDescr
                .getOperation(), oldDescr.getTimestamp());
        myDescription.write(newDescr);
    }
}
