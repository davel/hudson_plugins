package hudson.plugins.tasks.util.model;


import hudson.plugins.tasks.util.Messages;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A container for annotations.
 *
 * @author Ulli Hafner
 */
public abstract class AnnotationContainer implements AnnotationProvider, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 855696821788264261L;
    /** The hierarchy of a container. */
    public enum Hierarchy {
        /** Project level. */
        PROJECT,
        /** Module level. */
        MODULE,
        /** Package level. */
        PACKAGE,
        /** File level. */
        FILE}

    /** The annotations mapped by their key. */
    @SuppressWarnings("Se")
    private final Map<Long, FileAnnotation> annotations = new HashMap<Long, FileAnnotation>();
    /** The annotations mapped by priority. */
    private transient Map<Priority, Set<FileAnnotation>> annotationsByPriority;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, WorkspaceFile> filesByName;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, JavaPackage> packagesByName;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, MavenModule> modulesByName;

    /** Determines whether to build up a set of {@link WorkspaceFile}s. */
    @java.lang.SuppressWarnings("unused")
    private boolean handleFiles; // backward compatibility

    /** Name of this container. */
    private String name;
    /** Hierarchy level of this container. */
    private Hierarchy hierarchy;

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     *
     * @param hierarchy the hierarchy of this container
     */
    public AnnotationContainer(final Hierarchy hierarchy) {
        this(StringUtils.EMPTY, hierarchy);
    }

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     *
     * @param name the name of this container
     * @param hierarchy the hierarchy of this container
     */
    protected AnnotationContainer(final String name, final Hierarchy hierarchy) {
        initialize();
        this.name = name;
        this.hierarchy = hierarchy;
    }

    /**
     * Sets the hierarchy to the specified value.
     *
     * @param hierarchy the value to set
     */
    protected void setHierarchy(final Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * Returns the name of this container.
     *
     * @return the name of this container
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of this container.
     *
     * @param name the name of this container
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Initializes the transient mappings.
     */
    private void initialize() {
        annotationsByPriority = new EnumMap<Priority, Set<FileAnnotation>>(Priority.class);
        for (Priority priority : Priority.values()) {
            annotationsByPriority.put(priority, new HashSet<FileAnnotation>());
        }
        filesByName = new HashMap<String, WorkspaceFile>();
        packagesByName = new HashMap<String, JavaPackage>();
        modulesByName = new HashMap<String, MavenModule>();
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildMappings();
        return this;
    }

    /**
     * Rebuilds the priorities and files after deserialization.
     */
    protected void rebuildMappings() {
        initialize();
        for (FileAnnotation annotation : getAnnotations()) {
            updateMappings(annotation);
        }
    }

    /**
     * Updates the annotation drill-down mappings (priority, packages, files) with the specified annotation.
     *
     * @param annotation the new annotation
     */
    private void updateMappings(final FileAnnotation annotation) {
        annotationsByPriority.get(annotation.getPriority()).add(annotation);
        if (hierarchy == Hierarchy.PROJECT) {
            addModule(annotation);
        }
        if (hierarchy == Hierarchy.PROJECT || hierarchy == Hierarchy.MODULE) {
            addPackage(annotation);
        }
        if (hierarchy == Hierarchy.PROJECT || hierarchy == Hierarchy.MODULE || hierarchy == Hierarchy.PACKAGE) {
            addFile(annotation);
        }
    }

    /**
     * Adds a new module to this container that will contain the specified
     * annotation. If the module already exists, then the annotation is only added
     * to this module.
     *
     * @param annotation the new annotation
     */
    private void addModule(final FileAnnotation annotation) {
        String moduleName = annotation.getModuleName();
        if (!modulesByName.containsKey(moduleName)) {
            modulesByName.put(moduleName, new MavenModule(moduleName));
        }
        modulesByName.get(moduleName).addAnnotation(annotation);
    }

    /**
     * Adds a new package to this container that will contain the specified
     * annotation. If the package already exists, then the annotation is only added
     * to this package.
     *
     * @param annotation the new annotation
     */
    private void addPackage(final FileAnnotation annotation) {
        String packageName = annotation.getPackageName();
        if (!packagesByName.containsKey(packageName)) {
            packagesByName.put(packageName, new JavaPackage(packageName));
        }
        packagesByName.get(packageName).addAnnotation(annotation);
    }

    /**
     * Adds a new file to this container that will contain the specified
     * annotation. If the file already exists, then the annotation is only added
     * to this file.
     *
     * @param annotation the new annotation
     */
    private void addFile(final FileAnnotation annotation) {
        String fileName = annotation.getFileName();
        if (!filesByName.containsKey(fileName)) {
            filesByName.put(fileName, new WorkspaceFile(fileName));
        }
        filesByName.get(fileName).addAnnotation(annotation);
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation the annotation to add
     */
    public final void addAnnotation(final FileAnnotation annotation) {
        annotations.put(annotation.getKey(), annotation);
        updateMappings(annotation);
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final FileAnnotation[] newAnnotations) {
        addAnnotations(Arrays.asList(newAnnotations));
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations.values());
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return Collections.unmodifiableCollection(annotationsByPriority.get(priority));
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final String priority) {
        return getAnnotations(getPriority(priority));
    }

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority priority as a String
     *
     * @return enumeration value.
     */
    private Priority getPriority(final String priority) {
        return Priority.fromString(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations() {
        return annotations.size();
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationsByPriority.get(priority).size();
    }

    /** {@inheritDoc} */
    public final int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final Priority priority) {
        return !annotationsByPriority.get(priority).isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final String priority) {
        return hasAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final long key) {
        FileAnnotation annotation = annotations.get(key);
        if (annotation != null) {
            return annotation;
        }
        throw new NoSuchElementException("Annotation not found: key=" + key);
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final String key) {
        return getAnnotation(Long.parseLong(key));
    }

    /**
     * Returns a tooltip showing the distribution of priorities for this container.
     *
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip() {
        StringBuilder message = new StringBuilder();
        for (Priority priority : Priority.values()) {
            if (hasAnnotations(priority)) {
                message.append(priority.getLocalizedString());
                message.append(":");
                message.append(getNumberOfAnnotations(priority));
                message.append(" - ");
            }
        }
        return StringUtils.removeEnd(message.toString(), " - ");
    }


    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public final String getPackageCategoryName() {
        if (hasAnnotations()) {
            String fileName = getAnnotations().iterator().next().getFileName();
            if (fileName.endsWith(".cs")) {
                return Messages.NamespaceDetail_header();
            }
        }
        return Messages.PackageDetail_header();
    }

    /**
     * Gets the modules of this container that have annotations.
     *
     * @return the modules with annotations
     */
    public Collection<MavenModule> getModules() {
        return Collections.unmodifiableCollection(modulesByName.values());
    }

    /**
     * Returns whether the maven module with the given name exists.
     *
     * @param moduleName the module to check for
     *
     * @return <code>true</code> if the maven module with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsModule(final String moduleName) {
        return modulesByName.get(moduleName) != null;
    }

    /**
     * Gets the module with the given name.
     *
     * @param moduleName the name of the module
     *
     * @return the module with the given name
     */
    public MavenModule getModule(final String moduleName) {
        if (modulesByName.containsKey(moduleName)) {
            return modulesByName.get(moduleName);
        }
        throw new NoSuchElementException("Module not found: " + moduleName);
    }

    /**
     * Gets the packages of this container that have annotations.
     *
     * @return the packages with annotations
     */
    public Collection<JavaPackage> getPackages() {
        return Collections.unmodifiableCollection(packagesByName.values());
    }

    /**
     * Returns whether the package with the given name exists.
     *
     * @param packageName the package to check for
     *
     * @return <code>true</code> if the package with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsPackage(final String packageName) {
        return packagesByName.get(packageName) != null;
    }

    /**
     * Gets the package with the given name.
     *
     * @param packageName the name of the package
     *
     * @return the file with the given name
     */
    public JavaPackage getPackage(final String packageName) {
        if (packagesByName.containsKey(packageName)) {
            return packagesByName.get(packageName);
        }
        throw new NoSuchElementException("Package not found: " + packageName);
    }

    /**
     * Gets the files of this container that have annotations.
     *
     * @return the files with annotations
     */
    public Collection<WorkspaceFile> getFiles() {
        return Collections.unmodifiableCollection(filesByName.values());
    }

    /**
     * Returns whether the file with the given name exists.
     *
     * @param fileName the file to check for
     *
     * @return <code>true</code> if the file module with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsFile(final String fileName) {
        return filesByName.get(fileName) != null;
    }

    /**
     * Gets the file with the given name.
     *
     * @param fileName the short name of the file
     *
     * @return the file with the given name
     */
    public WorkspaceFile getFile(final String fileName) {
        if (filesByName.containsKey(fileName)) {
            return filesByName.get(fileName);
        }
        throw new NoSuchElementException("File not found: " + fileName);
    }

    /**
     * Gets the maximum number of annotations within the elements of the child hierarchy.
     *
     * @return the maximum number of annotations
     */
    public final int getAnnotationBound() {
        int maximum = 0;
        for (AnnotationContainer subContainer : getChildren()) {
            maximum = Math.max(maximum, subContainer.getNumberOfAnnotations());
        }
        return maximum;
    }

    /**
     * Returns the children containers of this container.
     * If we are already at the leaf level, then an empty collection is returned.
     *
     * @return the children containers of this container.
     */
    protected abstract Collection<? extends AnnotationContainer> getChildren();
}
