package hudson.plugins.tasks.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A serializable Java Bean class representing a Java package.
 */
public class JavaPackage extends AnnotationContainer implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 4034932648975191723L;
    /** Name of this package. */
    private final String name;
    /** All Java files in this package (mapped by their short name). */
    private final Map<String, WorkspaceFile> fileMapping = new HashMap<String, WorkspaceFile>();

    /**
     * Creates a new instance of <code>JavaPackage</code>.
     *
     * @param packageName
     *            the name of this package
     */
    public JavaPackage(final String packageName) {
        super();

        name = packageName;
    }

    /**
     * Returns the name of this package.
     *
     * @return the name of this package
     */
    public String getName() {
        return name;
    }

    /**
     * Creates the mapping of files.
     *
     * @param annotation
     *            the added annotation
     */
    @Override
    protected void annotationAdded(final FileAnnotation annotation) {
        WorkspaceFile file = annotation.getWorkspaceFile();

        fileMapping.put(file.getShortName(), file);
    }

    /**
     * Gets the files of this package that have annotations.
     *
     * @return the files with annotations
     */
    public Collection<WorkspaceFile> getFiles() {
        return Collections.unmodifiableCollection(fileMapping.values());
    }

    /**
     * Gets the file with the given name.
     *
     * @param fileName
     *            the short name of the file
     * @return the file with the given name
     */
    public WorkspaceFile getFile(final String fileName) {
        return fileMapping.get(fileName);
    }
}

