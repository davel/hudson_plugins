package hudson.plugins.mavensnapshottrigger;

import hudson.model.Cause;
import java.io.File;
import java.util.List;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class MavenSnapshotCause extends Cause {
    private List<File> modifications;

    public MavenSnapshotCause(List<File> modifications) {
        this.modifications = modifications;
    }

    @Override
    public String getShortDescription() {
        return Messages.MavenSnapshotCause_Description();
    }

    public List<File> getModifications() {
        return modifications;
    }
}
