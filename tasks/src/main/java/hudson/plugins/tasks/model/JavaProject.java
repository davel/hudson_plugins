package hudson.plugins.tasks.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a project that has been built by
 * Hudson.
 *
 * @author Ulli Hafner
 */
public class JavaProject extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 8556968267678442661L;
    /** All maven modules in this project (mapped by their name). */
    private final Map<String, MavenModule> moduleMapping = new HashMap<String, MavenModule>();
    /** Path of the workspace. */
    private String workspacePath;

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildPriorities();
        return this;
    }

    /**
     * Adds the specified annotations of the given files to this container.
     *
     * @param files
     *            the files to get the annotations from
     */
    public void addFiles(final Collection<WorkspaceFile> files) {
        for (WorkspaceFile workspaceFile : files) {
            addAnnotations(workspaceFile.getAnnotations());
        }
    }

    /**
     * Creates the mapping of modules.
     *
     * @param annotation
     *            the added annotation
     */
    @Override
    protected void annotationAdded(final FileAnnotation annotation) {
        WorkspaceFile file = annotation.getWorkspaceFile();

        String moduleName = StringUtils.defaultIfEmpty(file.getModuleName(), "Default Module");
        if (!moduleMapping.containsKey(moduleName)) {
            moduleMapping.put(moduleName, new MavenModule(moduleName));
        }
        moduleMapping.get(moduleName).addAnnotation(annotation);
    }

    /**
     * Gets the modules of this project that have annotations.
     *
     * @return the modules with annotations
     */
    public Collection<MavenModule> getModules() {
        return Collections.unmodifiableCollection(moduleMapping.values());
    }

    /**
     * Returns the maven module with the given name.
     *
     * @param moduleName
     *            the module to get
     * @return the module with the given name
     */
    public MavenModule getModule(final String moduleName) {
        MavenModule mavenModule = moduleMapping.get(moduleName);
        if (mavenModule != null) {
            return mavenModule;
        }
        throw new NoSuchElementException("Module not found: " + moduleName);
    }

    /**
     * Gets the packages of this project that have annotations.
     *
     * @return the packages with annotations
     */
    public Collection<JavaPackage> getPackages() {
        List<JavaPackage> packages = new ArrayList<JavaPackage>();
        for (MavenModule module : moduleMapping.values()) {
            packages.addAll(module.getPackages());
        }
        return packages;
    }

    /**
     * Returns the package with the given name. This method is only valid for
     * single module projects.
     *
     * @param name
     *            the package name
     * @return the package with the given name.
     */
    public JavaPackage getPackage(final String name) {
        return getSingleModule().getPackage(name);
    }

    /**
     * Gets the files of this project that have annotations.
     *
     * @return the files with annotations
     */
    public Collection<WorkspaceFile> getFiles() {
        List<WorkspaceFile> files = new ArrayList<WorkspaceFile>();
        for (MavenModule module : moduleMapping.values()) {
            files.addAll(module.getFiles());
        }
        return files;
    }

    /**
     * Returns the file with the given name. This method is only valid for
     * single module projects.
     *
     * @param name the file name
     * @return the file with the given name.
     */
    public WorkspaceFile getFile(final String name) {
        return getSingleModule().getFile(name);
    }

    /**
     * Returns the single module of this project.
     *
     * @return the module of this project
     */
    private MavenModule getSingleModule() {
        if (moduleMapping.size() != 1) {
            throw new IllegalArgumentException("Number of modules != 1");
        }
        return moduleMapping.values().iterator().next();
    }

    /**
     * Sets the root path of the workspace files.
     *
     * @param workspacePath path to workspace
     */
    public void setWorkspacePath(final String workspacePath) {
        this.workspacePath = workspacePath;
    }

    /**
     * Returns the root path of the workspace files.
     *
     * @return the workspace path
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    /**
     * Gets the maximum number of tasks in a module.
     *
     * @return the maximum number of tasks
     */
    public int getTaskBound() {
        int tasks = 0;
        for (MavenModule module : moduleMapping.values()) {
            tasks = Math.max(tasks, module.getNumberOfAnnotations());
        }
        return tasks;
    }
}

