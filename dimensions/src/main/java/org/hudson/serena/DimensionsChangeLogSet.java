package org.hudson.serena;

import hudson.model.User;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stores update path and Dimensions Item object key.
 *
 * @author Jose Noheda [jose.noheda@gmail.com]
 */
public final class DimensionsChangeLogSet extends ChangeLogSet<ChangeLogSet.Entry> {

    private static final Logger LOGGER = Logger.getLogger(DimensionsChangeLogSet.class.getName());

    private List<ChangeLogSet.Entry> items = new ArrayList<Entry>();

    public DimensionsChangeLogSet(final AbstractBuild build, final File changelogFile) {
        super(build);
        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(changelogFile));
            while((line = reader.readLine()) != null) {
                if (line.indexOf("Updated") >= 0) {
                    final String path = line.substring(line.indexOf('\'') + 1, line.indexOf("' using"));
                    final String item = line.substring(line.indexOf("Item"));
                    items.add(new ChangeLogSet.Entry() {

                        @Override public String getMsg() {
                            return "Updated [" + path + "];" + item;
                        }

                        @Override public User getAuthor() {
                            return null;
                        }

                        @Override public Collection<String> getAffectedPaths() {
                            return Arrays.asList(path);
                        }

                    });
                }
            }
        } catch (IOException ioe) {
            LOGGER.warning("Unexpected exception reading Dimensions change log file: " + ioe.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                LOGGER.warning("Could not close Dimensions change log file: " + ioe.getMessage());
            }
        }
    }

    @Override public boolean isEmptySet() {
        return items.size() == 0;
    }

    @Override public Iterator iterator() {
        return items.iterator();
    }

}
