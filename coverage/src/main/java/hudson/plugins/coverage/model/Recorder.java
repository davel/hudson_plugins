package hudson.plugins.coverage.model;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 27-Jun-2008 17:13:08
 */
public interface Recorder extends Serializable {

    /**
     * Find all the source code files.
     *
     * @param root The instance to record results against.
     */
    void identifySourceFiles(Instance root);

    /**
     * Rebuild the instance tree from the provided measurementFiles.
     *
     * @param root                The instance to rebuild the results against.
     * @param measurementFiles    The measurement files that were identified the first time the tree was built.
     * @param sourceCodeDirectory The root of the source code directory.
     */
    void reidentifySourceFiles(Instance root, Set<File> measurementFiles, File sourceCodeDirectory);

    /**
     * Parse the source file results, populating any sub-file level results.
     *
     * @param sourceFile      The source file instance to parse the results for.
     * @param measurementFile The file that the memo objects relate to.
     * @param memos           The memo objects that the recorder registered against this instance when it was attached.
     */
    void parseSourceResults(Instance sourceFile, File measurementFile, Collection<Object> memos);

}
