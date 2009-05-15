package hudson.plugins.javanet_uploader;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.jnt.FileStatus;
import hudson.model.Descriptor;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.Extension;

/**
 * Instruction of how to upload one file.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Entry implements Describable<Entry> {
    /**
     * Destination of the copy. If the source file matches
     * multiple files, this will be treated as a directory.
     */
    public final String filePath;
    public final String description;
    public final FileStatus status;
    /**
     * File name relative to the workspace root to upload.
     * <p>
     * May contain macro, wildcard.
     */
    public final String sourceFile;

    @DataBoundConstructor
    public Entry(String filePath, String description, FileStatus status, String sourceFile) {
        this.filePath = filePath;
        this.description = description;
        this.status = status;
        this.sourceFile = sourceFile;
    }


    // use Descriptor just to make form binding work
    public Descriptor<Entry> getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(EntryDescriptor.class);
    }

    @Extension
    public static final class EntryDescriptor extends Descriptor<Entry> {
        public String getDisplayName() {
            return "";
        }
    }
}
