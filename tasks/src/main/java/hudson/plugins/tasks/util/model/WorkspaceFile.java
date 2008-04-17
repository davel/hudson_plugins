package hudson.plugins.tasks.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a file in the Hudson workspace.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class WorkspaceFile extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 601361940925156719L;
    /** The absolute filename of this file. */
    private final String name;
    /** This file. */
    private final List<WorkspaceFile> files;

    /**
     * Creates a new instance of <code>WorkspaceFile</code>.
     *
     * @param fileName
     *            absolute path of this file
     */
    public WorkspaceFile(final String fileName) {
        super(false);

        name = fileName.replace('\\', '/');
        List<WorkspaceFile> singleFile = new ArrayList<WorkspaceFile>();
        singleFile.add(this);
        files = Collections.unmodifiableList(singleFile);
    }

    /**
     * Returns the filename name of this file.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a readable name of this workspace file without path prefix.
     *
     * @return a readable name of this workspace file.
     */
    public String getShortName() {
        return StringUtils.substringAfterLast(name, "/");
    }

    /**
     * Rebuilds the bidirectional links between the annotations and this
     * workspace file after deserialization.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildMappings(false);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<WorkspaceFile> getFiles() {
        return files;
    }

    /** {@inheritDoc} */
    @Override
    public WorkspaceFile getFile(final String fileName) {
        if (getName().equals(fileName)) {
            return this;
        }
        throw new NoSuchElementException("File not found: " + fileName);
    }
}

