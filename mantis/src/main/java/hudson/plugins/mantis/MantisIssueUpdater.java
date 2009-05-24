package hudson.plugins.mantis;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.tasks.Publisher;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Parses changelog for Mantis issue IDs and updates Mantis issues.
 * 
 * @author Seiji Sogabe
 */
public final class MantisIssueUpdater extends Publisher {

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private final boolean keepNotePrivate;

    private final boolean recordChangelog;
    
    @DataBoundConstructor
    public MantisIssueUpdater(final boolean keepNotePrivate, final boolean recordChangelog) {
        this.keepNotePrivate = keepNotePrivate;
        this.recordChangelog = recordChangelog;
    }

    public boolean isKeepNotePrivate() {
        return keepNotePrivate;
    }

    public boolean isRecordChangelog() {
        return recordChangelog;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        final Updater updater = new Updater(this);
        return updater.perform(build, listener);
    }

    public static final class DescriptorImpl extends Descriptor<Publisher> {

        private DescriptorImpl() {
            super(MantisIssueUpdater.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.MantisIssueUpdater_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/mantis/help.html";
        }

        @Override
        public Publisher newInstance(final StaplerRequest req, final JSONObject formData) {
            // only administrator allowed
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
            return req.bindJSON(MantisIssueUpdater.class, formData);
        }
    }
}
