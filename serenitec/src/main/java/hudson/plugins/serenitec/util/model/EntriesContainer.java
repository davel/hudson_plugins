/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.10 $
 * @since $Date: 2008/07/23 12:05:05 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util.model;

import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.plugins.serenitec.parseur.ReportPointeur;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

/**
 * A container for annotations.
 * 
 * @author Ulli Hafner
 */
public abstract class EntriesContainer implements EntriesProvider, Serializable
{

    /** Unique identifier of this class. */
    private static final long serialVersionUID = 855696821788264261L;

    

    /** The hierarchy of a container. */
    public enum Hierarchy
    {

        /** Project level. */
        PROJECT,
        /** Module level. */
        MODULE,
        /** Package level. */
        PACKAGE,
        /** File level. */
        FILE
    }
    @SuppressWarnings("Se")
    private final List<ReportEntry> rules = new ArrayList<ReportEntry>();
    /** The active entries */
    private final List<ReportEntry> entries = new ArrayList<ReportEntry>();
    /** The entries mapped by severity. */
    private transient Map<Integer, ArrayList<ReportEntry>> entriesBySeverity;
    /** The entries mapped by name. */
    private transient Map<String, ArrayList<ReportEntry>> entriesByName;
    /** The not fixed entries */
    private transient List<ReportEntry> entriesNotFixed;
    /** The fixed entries */
    private transient List<ReportEntry> entriesFixed;
    /** Entries mapped by number of pointeurs */
    private transient List<ReportEntry> entriesOrderByNumberOfPointeurs;
    /** The TOP5 entries */
    private transient List<ReportEntry> topFiveEntries;
    /** The list of pointeurs */
    private transient List<ReportPointeur> pointeurs;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, Package> packagesByName;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, MavenModule> modulesByName;
    /** Name of this container. */
    private String name;
    /** Hierarchy level of this container. */
    private Hierarchy hierarchy;

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     * 
     * @param hierarchy
     *            the hierarchy of this container
     */
    public EntriesContainer(final Hierarchy hierarchy)
    {
        this(StringUtils.EMPTY, hierarchy);
    }

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     * 
     * @param name
     *            the name of this container
     * @param hierarchy
     *            the hierarchy of this container
     */
    protected EntriesContainer(final String name, final Hierarchy hierarchy)
    {

        initialize();
        this.name = name;
        this.hierarchy = hierarchy;
    }

    /**
     * Adds a new category to this container that will contain the specified annotation. If the category already exists, then the annotation
     * is only added to this category.
     * 
     * @param annotation
     *            the new annotation
     */
    private void addCategory(final ReportEntry annotation)
    {        // String category = annotation.getCategory();
        // if (!annotationsByCategory.containsKey(category)) {
        // annotationsByCategory.put(category, new HashSet<FileAnnotation>());
        // }
        // annotationsByCategory.get(category).add(annotation);
    }

    /**
     * Adds the specified annotations to this container.
     * 
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addEntries(final Collection<? extends ReportEntry> newentry)
    {
        for (final ReportEntry entry : newentry)
        {
            addEntry(entry);
        }
        initialize();
    }

    /**
     * Adds the specified annotations to this container.
     * 
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addEntries(final ReportEntry[] newAnnotations)
    {
        addEntries(Arrays.asList(newAnnotations));
    }

    /**
     * Adds the specified annotation to this container.
     * 
     * @param annotation
     *            the annotation to add
     */
    public final void addEntry(final ReportEntry entry)
    {
        rules.add(entry);
        if (entry.isActive())
        {
            entries.add(entry);
        }
        updateMappings(entry);
    }

    /**
     * Adds a new file to this container that will contain the specified annotation. If the file already exists, then the annotation is only
     * added to this file.
     * 
     * @param annotation
     *            the new annotation
     */
    private void addFile(final ReportEntry annotation)
    {        // String fileName = annotation.getFileName();
        // if (!filesByName.containsKey(fileName)) {
        // filesByName.put(fileName, new WorkspaceFile(fileName));
        // }
        // filesByName.get(fileName).addAnnotation(annotation);
    }

    /**
     * Adds a new module to this container that will contain the specified annotation. If the module already exists, then the annotation is
     * only added to this module.
     * 
     * @param annotation
     *            the new annotation
     */
    private void addModule(final ReportEntry annotation)
    {        // String moduleName = annotation.getModuleName();
        // if (!modulesByName.containsKey(moduleName)) {
        // modulesByName.put(moduleName, new MavenModule(moduleName));
        // }
        // modulesByName.get(moduleName).addAnnotation(annotation);
    }

    /**
     * Adds a new package to this container that will contain the specified annotation. If the package already exists, then the annotation
     * is only added to this package.
     * 
     * @param annotation
     *            the new annotation
     */
    private void addPackage(final ReportEntry annotation)
    {        // String packageName = annotation.getPackageName();
        // if (!packagesByName.containsKey(packageName)) {
        // packagesByName.put(packageName, new Package(packageName));
        // }
        // packagesByName.get(packageName).addAnnotation(annotation);
    }

    /**
     * Adds a new type to this container that will contain the specified annotation. If the type already exists, then the annotation is only
     * added to this type.
     * 
     * @param annotation
     *            the new annotation
     */
    private void addType(final ReportEntry annotation)
    {        // String type = annotation.getType();
        // if (!annotationsByType.containsKey(type)) {
        // annotationsByType.put(type, new HashSet<FileAnnotation>());
        // }
        // annotationsByType.get(type).add(annotation);
    }

    /**
     * Returns whether the maven module with the given name exists.
     * 
     * @param moduleName
     *            the module to check for
     * @return <code>true</code> if the maven module with the given name exists, <code>false</code> otherwise
     */
    public boolean containsModule(final String moduleName)
    {
        return modulesByName.containsKey(moduleName);
    }

    /**
     * Returns whether the package with the given name exists.
     * 
     * @param packageName
     *            the package to check for
     * @return <code>true</code> if the package with the given name exists, <code>false</code> otherwise
     */
    public boolean containsPackage(final String packageName)
    {
        return packagesByName.containsKey(packageName);
    }

    /**
     * Gets the maximum number of annotations within the elements of the child hierarchy.
     * 
     * @return the maximum number of annotations
     */
    public final int getAnnotationBound()
    {

        int maximum = 0;
        for (final EntriesContainer subContainer : getChildren())
        {
            maximum = Math.max(maximum, subContainer.getNumberOfEntry());
        }
        return maximum;
    }

    /** {@inheritDoc} */
    public final Collection<ReportEntry> getAnnotations()
    {

        return Collections.unmodifiableCollection(rules);
    }

    /**
     * Returns the children containers of this container. If we are already at the leaf level, then an empty collection is returned.
     * 
     * @return the children containers of this container.
     */
    protected abstract Collection<? extends EntriesContainer> getChildren();

    /**
     * Returns this container.
     * 
     * @return this container
     */
    public EntriesContainer getContainer()
    {

        return this;
    }

    /**
     * Returns the pointeur identified by its key number
     * @param key
     * @return ReportPointeur
     */
    public ReportPointeur getPointeur(int key)
    {
        for (ReportPointeur pointeur : pointeurs)
        {
            if (pointeur.getKey() == key)
            {
                return pointeur;
            }
        }
        return null;
    }
    public List<String> getModifiedFiles()
    {
        List<String> resultat = new ArrayList<String>();
        for (ReportPointeur pointeur : pointeurs)
        {
            if (!resultat.contains(pointeur.getFilename()))
            {
                resultat.add(pointeur.getFilename());
            }
        }
        return resultat;
    }
    public final List<ReportEntry> getRules()
    {
        return rules;
    }
    

    public final List<ReportEntry> getEntries()
    {
        return entries;
    }

    public final Map<String, ArrayList<ReportEntry>> getEntriesByName()
    {
        return entriesByName;
    }

    public final Map<Integer, ArrayList<ReportEntry>> getEntriesBySeverity()
    {
        return entriesBySeverity;
    }

    public final List<ReportEntry> getEntriesFixed()
    {
        return entriesFixed;
    }

    public List<ReportEntry> getEntriesNotFixed()
    {
        return entriesNotFixed;
    }

    /**
     * @return the hightest severity entry discovered
     */
    public int getMaxSeverityDiscovered()
    {
        int resultat = 0;
        if (getNumberOfSeverityHighSecurity() > 0)
        {
            resultat = 5;
        }
        else if (getNumberOfSeverityLowSecurity() > 0)
        {
            resultat = 4;
        }
        else if (getNumberOfSeverityDesign() > 0)
        {
            resultat = 3;
        }
        else if (getNumberOfSeverityPerformance() > 0)
        {
            resultat = 2;
        }
        else if (getNumberOfSeverityFormatage() > 0)
        {
            resultat = 1;
        }
        return resultat;
    }

    /**
     * Gets the module with the given name.
     * 
     * @param moduleName
     *            the name of the module
     * @return the module with the given name
     */
    public MavenModule getModule(final String moduleName)
    {

        if (modulesByName.containsKey(moduleName))
        {
            return modulesByName.get(moduleName);
        }
        throw new NoSuchElementException("Module not found: " + moduleName);
    }

    /**
     * Gets the modules of this container that have annotations.
     * 
     * @return the modules with annotations
     */
    public Collection<MavenModule> getModules()
    {

        return Collections.unmodifiableCollection(modulesByName.values());
    }

    public final Map<String, MavenModule> getModulesByName()
    {

        return modulesByName;
    }

    /**
     * Returns the name of this container.
     * 
     * @return the name of this container
     */
    public final String getName()
    {

        return name;
    }

    public int getNumberOfRules()
    {
        return rules.size();
    }

    public int getNumberOfEntry()
    {
        return entries.size();
    }

    public int getNumberOfFixedEntry()
    {
        return entriesFixed.size();
    }

    public int getNumberOfNotFixedEntry()
    {
        return entriesNotFixed.size();
    }

    public int getNumberOfPointeurs()
    {
        System.out.println("EntriesContainer.getNumberOfPointeurs : " + pointeurs.size());
        return pointeurs.size();
    }

    public int getNumberOfSeverityDesign()
    {
        if (entriesBySeverity.containsKey(3))
        {
            return entriesBySeverity.get(3).size();
        }
        else
        {
            return 0;
        }
    }

    public int getNumberOfSeverityFormatage()
    {

        if (entriesBySeverity.containsKey(1))
        {
            return entriesBySeverity.get(1).size();
        }
        else
        {
            return 0;
        }
    }

    public int getNumberOfSeverityHighSecurity()
    {
        if (entriesBySeverity.containsKey(5))
        {
            return entriesBySeverity.get(5).size();
        }
        else
        {
            return 0;
        }
    }

    public int getNumberOfSeverityLowSecurity()
    {

        if (entriesBySeverity.containsKey(4))
        {
            return entriesBySeverity.get(4).size();
        }
        else
        {
            return 0;
        }
    }

    public int getNumberOfSeverityPerformance()
    {

        if (entriesBySeverity.containsKey(2))
        {
            return entriesBySeverity.get(2).size();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Gets the package with the given name.
     * 
     * @param packageName
     *            the name of the package
     * @return the file with the given name
     */
    public Package getPackage(final String packageName)
    {

        if (packagesByName.containsKey(packageName))
        {
            return packagesByName.get(packageName);
        }
        throw new NoSuchElementException("Package not found: " + packageName);
    }

    /**
     * Returns the package category name for the scanned files. Currently, only java and c# files are supported.
     * 
     * @return the package category name for the scanned files
     */
    public final String getPackageCategoryName()
    {

        return "Entete message header getpackageCategoryName";
    }

    /**
     * Gets the packages of this container that have annotations.
     * 
     * @return the packages with annotations
     */
    public Collection<Package> getPackages()
    {

        return Collections.unmodifiableCollection(packagesByName.values());
    }

    public final List<ReportPointeur> getPointeurs()
    {
        return pointeurs;
    }

    /**
     * Returns a tooltip showing the distribution of priorities for this container.
     * 
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip()
    {

        final StringBuilder message = new StringBuilder();

        return StringUtils.removeEnd("repartition selon les priorités des entry", " - ");
    }

    public final List<ReportEntry> getTopFiveEntries()
    {
        return topFiveEntries;
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations()
    {
        System.out.println("EntriesContainer.HASANNOTATION()**");
        return !entries.isEmpty();
    }

    /**
     * Initializes the transient mappings.
     */
    private void initialize()
    {
        entriesBySeverity = new Hashtable<Integer, ArrayList<ReportEntry>>();
        entriesByName = new Hashtable<String, ArrayList<ReportEntry>>();
        entriesNotFixed = new ArrayList<ReportEntry>();
        entriesFixed = new ArrayList<ReportEntry>();
        topFiveEntries = new ArrayList<ReportEntry>();
        entriesOrderByNumberOfPointeurs = new ArrayList<ReportEntry>();
        pointeurs = new ArrayList<ReportPointeur>();

        boolean etat_pointeur;
        for (final ReportEntry entry : entries)
        {
            /**
             * ENTRIES BY SEVERITY
             */
            if (entriesBySeverity.containsKey(entry.getSeverity()))
            {
                final ArrayList<ReportEntry> temp = entriesBySeverity.get(entry.getSeverity());
                temp.add(entry);
                entriesBySeverity.put(entry.getSeverity(), temp);
            }
            else
            {
                final ArrayList<ReportEntry> temp = new ArrayList<ReportEntry>();
                temp.add(entry);
                entriesBySeverity.put(entry.getSeverity(), temp);
            }
            /**
             * ENTRIES BY NAME
             */
            if (entriesByName.containsKey(entry.getName()))
            {
                final ArrayList<ReportEntry> temp = entriesByName.get(entry.getName());
                temp.add(entry);
                entriesByName.put(entry.getName(), temp);
            }
            /**
             * ENTRIES NOT FIXED & ENTRIES FIXED
             */
            etat_pointeur = true;
            for (final ReportPointeur pointeur : entry.getPointeurs())
            {
                if (!pointeur.isIsfixed())
                {
                    etat_pointeur = false;
                }
                /**
                 * POINTEURS
                 */
                pointeurs.add(pointeur);
            }
            if (etat_pointeur)
            {
                entriesFixed.add(entry);
            }
            else
            {
                entriesNotFixed.add(entry);
            }
        }
        /**
         * TOP FIVE ENTRIES
         */
        entriesOrderByNumberOfPointeurs = rules;
        Collections.sort(entriesOrderByNumberOfPointeurs, Collections.reverseOrder());
        int i = 0;
        while (i < 5 && i < entriesOrderByNumberOfPointeurs.size())
        {
            topFiveEntries.add(entriesOrderByNumberOfPointeurs.get(i));
            i++;
        }

        packagesByName = new HashMap<String, Package>();
        modulesByName = new HashMap<String, MavenModule>();
    }

    /**
     * Return true if all the patterns have been fixed
     */
    public final boolean IsFixed()
    {
        return getNumberOfNotFixedEntry() == 0;
    }

    /**
     * Rebuilds the priorities mapping.
     * 
     * @return the created object
     */
    private Object readResolve()
    {

        rebuildMappings();
        return this;
    }

    /**
     * Rebuilds the priorities and files after deserialization.
     */
    protected void rebuildMappings()
    {

        initialize();
        for (final ReportEntry entry : getEntries())
        {
            updateMappings(entry);
        }
    }

    /**
     * Sets the hierarchy to the specified value.
     * 
     * @param hierarchy
     *            the value to set
     */
    protected void setHierarchy(final Hierarchy hierarchy)
    {

        this.hierarchy = hierarchy;
    }

    /**
     * Sets the name of this container.
     * 
     * @param name
     *            the name of this container
     */
    public final void setName(final String name)
    {

        this.name = name;
    }

    /**
     * Updates the annotation drill-down mappings (priority, packages, files) with the specified annotation.
     * 
     * @param annotation
     *            the new annotation
     */
    private void updateMappings(final ReportEntry annotation)
    {        // annotationsByPriority.get(annotation.getPriority()).add(annotation);
        // if (StringUtils.isNotBlank(annotation.getCategory())) {
        // addCategory(annotation);
        // }
        // if (StringUtils.isNotBlank(annotation.getType())) {
        // addType(annotation);
        // }
        // if (hierarchy == Hierarchy.PROJECT) {
        // addModule(annotation);
        // }
        // if (hierarchy == Hierarchy.PROJECT || hierarchy == Hierarchy.MODULE) {
        // addPackage(annotation);
        // }
        // if (hierarchy == Hierarchy.PROJECT || hierarchy == Hierarchy.MODULE || hierarchy == Hierarchy.PACKAGE) {
        // addFile(annotation);
        // }
    }
}
