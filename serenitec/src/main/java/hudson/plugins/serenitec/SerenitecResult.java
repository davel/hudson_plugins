/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.6 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;


import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.plugins.serenitec.parseur.ReportPointeur;
import hudson.plugins.serenitec.util.ErrorDetails;
import hudson.plugins.serenitec.util.Project;
import hudson.plugins.serenitec.util.ProjectDetails;
import hudson.plugins.serenitec.util.ResultAction;
import hudson.plugins.serenitec.util.SeverityDetails;
import hudson.plugins.serenitec.util.SourceDetail;
import hudson.plugins.serenitec.util.model.EntriesContainer;
import hudson.plugins.serenitec.util.model.EntriesProvider;
import hudson.plugins.serenitec.util.model.EntryStream;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.thoughtworks.xstream.XStream;

/**
 * Represents the results of the warning analysis. One instance of this class is persisted for each build via an XML file.
 * 
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class SerenitecResult implements ModelObject, Serializable, EntriesProvider
{

    /** Unique identifier of this class. */
    private static final long    serialVersionUID  = 2768250056765266658L;
    private static final float   PERCENTAGE_100_0F = 100.0f;
    /** Serialization provider. */
    private static final XStream XSTREAM           = new EntryStream();
    static {
        XSTREAM.alias("Entry", ReportEntry.class);
    }

    /**
     * Compute the percentage ratio between two numbers.
     * 
     * @param _n1
     * @param _n2
     * @return
     */
    public static float doPercentage(final float _n1, final float _n2) {

        return _n1 * SerenitecResult.PERCENTAGE_100_0F / _n2;
    }

    /** The parsed warnings result. */
    @SuppressWarnings("Se")
    private transient WeakReference<Project> projectSerenitec;
    private Project                          save_project;
    /** All the entries */
    private List<ReportEntry>                rules;
    /** All the active entries */
    private List<ReportEntry>                entries;
    /** The entries not fixed */
    private List<ReportEntry>                entriesnotfixed;
    /** The fixed entries */
    private List<ReportEntry>                entriesfixed;
    /** The new entries */
    private List<ReportEntry>                newEntries;
    /** The pointeurs */
    private List<ReportPointeur>             pointeurs;
    /** The modified files */
    private List<String>                     modifiedFiles;
    /** The number of entry in this build */
    private int                              numberOfEntry;
    /** the number of entry in the previous build */
    private int                              numberOfEntryBefore;
    /** The top 5 most present entry */
    private List<ReportEntry>                topFiveEntries;

    private List<ReportEntry>                topFiveEntriesBefore;
    /** The number of new entry in this build. */
    private int                              numberOfNewEntry;
    private int                              numberOfNewEntryBefore;
    private float                            numberOfNewEntryPercent;
    /** The number of fixed warnings in this build. */
    private int                              numberOfFixedEntry;
    private int                              numberOfFixedEntryBefore;
    private float                            numberOfFixedEntryPercent;
    /** The number of not fixed warnings in this build */
    private int                              numberOfNotFixedEntry;
    private int                              numberOfNotFixedEntryBefore;
    private float                            numberOfNotFixedEntryPercent;
    /** Difference between this and the previous build. */
    private int                              delta;
    private int                              numberOfSeverityFormatage;
    private int                              numberOfSeverityFormatagePatterns;
    private float                            numberOfSeverityFormatagePercent;
    private int                              numberOfSeverityPerformance;
    private int                              numberOfSeverityPerformancePatterns;
    private float                            numberOfSeverityPerformancePercent;
    private int                              numberOfSeverityDesign;
    private int                              numberOfSeverityDesignPatterns;
    private float                            numberOfSeverityDesignPercent;
    private int                              numberOfSeverityLowSecurity;
    private int                              numberOfSeverityLowSecurityPatterns;
    private float                            numberOfSeverityLowSecurityPercent;
    private int                              numberOfSeverityHighSecurity;
    private int                              numberOfSeverityHighSecurityPatterns;
    private float                            numberOfSeverityHighSecurityPercent;
    private int                              numberOfSeverityFormatageBefore;
    private int                              numberOfSeverityPerformanceBefore;
    private int                              numberOfSeverityDesignBefore;
    private int                              numberOfSeverityLowSecurityBefore;

    private int                              numberOfSeverityHighSecurityBefore;
    /** Number of pointeur */
    private int                              numberOfPointeurs;

    private float                            numberMoyenneOfPointeursPerEntry;

    /** Error messages. */
    @SuppressWarnings("Se")
    private List<String>                     errors;

    /** Current build as owner of this action. */
    @SuppressWarnings("Se")
    private AbstractBuild<?, ?>              owner;
    /** The modules with no warnings. */
    @SuppressWarnings("Se")
    /** The total number of modules with or without warnings. */
    private int                              numberOfModules;

    private int                              numberOfPointeursBefore;
    private int                              numberOfRules;
    private int                              numberOfRulesBefore;

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     * 
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     */
    public SerenitecResult(final AbstractBuild<?, ?> build, final Project project) {

        this(build, project, new Project());
    }
    /**
     * Creates a new instance of <code>SerenitecResult</code>.
     * 
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     * @param previous
     *            the result of the previous build
     */
    public SerenitecResult(final AbstractBuild<?, ?> build, final Project project, final SerenitecResult previous) {

        EntriesContainer previousProject = previous.getProject();
        System.out
                .println("Lancement de initialize() par SerenitecResult(final AbstractBuild<?, ?> build, final Project project, final SerenitecResult previous)");
        initialize(build, project, previousProject);
        System.out.println("Fin du lancementy");

    }

    /**
     * Creates a new instance of <code>SerenitecResult</code>.
     * 
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     * @param previousProject
     *            the parsed warnings result of the previous build
     */
    public SerenitecResult(final AbstractBuild<?, ?> build, final Project project, final EntriesContainer previousProject) {

        System.out
                .println("Executing SerenitecResult.SerenitecResult(final AbstractBuild<?, ?> build, final Project project, final EntriesContainer previousProject)");
        initialize(build, project, previousProject);
    }
    public void initialize(final AbstractBuild<?, ?> build, final Project project, final EntriesContainer previousProject) {

        System.out.println("Executing SerenitecResult.initialize()");

        owner = build;
        errors = new ArrayList<String>();

        save_project = project;
        /**
         * Get the activated rules
         */
        rules = project.getContainer().getRules();
        numberOfRules = rules.size();

        /**
         * Get the entries
         */
        entries = project.getContainer().getEntries();
        numberOfEntry = project.getContainer().getNumberOfEntry();

        /**
         * Get all the modified files
         */
        modifiedFiles = project.getContainer().getModifiedFiles();

        /**
         * Get the not fixed entries
         */
        entriesnotfixed = project.getContainer().getEntriesNotFixed();
        numberOfNotFixedEntry = entriesnotfixed.size();
        numberOfNotFixedEntryPercent = doPercentage(numberOfNotFixedEntry, numberOfEntry);

        /**
         * Get the fixed errors
         */
        entriesfixed = project.getContainer().getEntriesFixed();
        numberOfFixedEntry = entriesfixed.size();
        numberOfFixedEntryPercent = doPercentage(numberOfFixedEntry, numberOfEntry);

        /**
         * Get the top five entries
         */
        topFiveEntries = project.getContainer().getTopFiveEntries();

        /**
         * Get the number of patterns
         */
        pointeurs = project.getContainer().getPointeurs();
        numberOfPointeurs = project.getContainer().getNumberOfPointeurs();
        numberMoyenneOfPointeursPerEntry = numberOfPointeurs / numberOfEntry;

        /**
         * Severity analysis
         */
        if (project.getContainer().getEntriesBySeverity().containsKey(1)) {
            numberOfSeverityFormatage = project.getContainer().getNumberOfSeverityFormatage();
            numberOfSeverityFormatagePatterns = project.getContainer().getNumberOfSeverityFormatagePatterns();
        } else {
            numberOfSeverityFormatage = 0;
            numberOfSeverityFormatagePatterns = 0;
        }

        if (project.getContainer().getEntriesBySeverity().containsKey(2)) {
            numberOfSeverityPerformance = project.getContainer().getNumberOfSeverityPerformance();
            numberOfSeverityPerformancePatterns = project.getContainer().getNumberOfSeverityPerformancePatterns();
        } else {
            numberOfSeverityPerformance = 0;
            numberOfSeverityPerformancePatterns = 0;
        }

        if (project.getContainer().getEntriesBySeverity().containsKey(3)) {
            numberOfSeverityDesign = project.getContainer().getNumberOfSeverityDesign();
            numberOfSeverityDesignPatterns = project.getContainer().getNumberOfSeverityDesignPatterns();
        } else {
            numberOfSeverityDesign = 0;
            numberOfSeverityDesignPatterns = 0;
        }

        if (project.getContainer().getEntriesBySeverity().containsKey(4)) {
            numberOfSeverityLowSecurity = project.getContainer().getNumberOfSeverityLowSecurity();
            numberOfSeverityLowSecurityPatterns = project.getContainer().getNumberOfSeverityLowSecurityPatterns();
        } else {
            numberOfSeverityLowSecurity = 0;
            numberOfSeverityLowSecurityPatterns = 0;
        }

        if (project.getContainer().getEntriesBySeverity().containsKey(5)) {
            numberOfSeverityHighSecurity = project.getContainer().getNumberOfSeverityHighSecurity();
            numberOfSeverityHighSecurityPatterns = project.getContainer().getNumberOfSeverityHighSecurityPatterns();
        } else {
            numberOfSeverityHighSecurity = 0;
            numberOfSeverityHighSecurityPatterns = 0;
        }
        numberOfSeverityFormatagePercent = doPercentage(numberOfSeverityFormatage, numberOfEntry);
        numberOfSeverityPerformancePercent = doPercentage(numberOfSeverityPerformance, numberOfEntry);
        numberOfSeverityDesignPercent = doPercentage(numberOfSeverityDesign, numberOfEntry);
        numberOfSeverityLowSecurityPercent = doPercentage(numberOfSeverityLowSecurity, numberOfEntry);
        numberOfSeverityHighSecurityPercent = doPercentage(numberOfSeverityHighSecurity, numberOfEntry);

        /**
         * get stats from the last build
         */
        System.out.println("if (previousProject.hasAnnotations())");
        if (previousProject.hasAnnotations()) {
            numberOfRulesBefore = previousProject.getContainer().getRules().size();
            numberOfPointeursBefore = previousProject.getContainer().getNumberOfPointeurs();
            numberOfEntryBefore = previousProject.getContainer().getNumberOfEntry();

            numberOfNotFixedEntryBefore = previousProject.getContainer().getEntriesNotFixed().size();
            numberOfFixedEntryBefore = previousProject.getContainer().getEntriesFixed().size();
            topFiveEntriesBefore = previousProject.getContainer().getTopFiveEntries();
            numberOfSeverityFormatageBefore = previousProject.getContainer().getNumberOfSeverityFormatage();
            numberOfSeverityPerformanceBefore = previousProject.getContainer().getNumberOfSeverityPerformance();
            numberOfSeverityDesignBefore = previousProject.getContainer().getNumberOfSeverityDesign();
            numberOfSeverityLowSecurityBefore = previousProject.getContainer().getNumberOfSeverityLowSecurity();
            numberOfSeverityHighSecurityBefore = previousProject.getContainer().getNumberOfSeverityHighSecurity();
            /**
             * Get the new entries
             */
            newEntries = new ArrayList<ReportEntry>();
            for (final ReportEntry entry : rules) {
                if (previousProject != project && previousProject.getContainer().getEntries().contains(entry.getName())) {
                    newEntries.add(entry);
                }
            }
            numberOfNewEntry = newEntries.size();
            numberOfNewEntryPercent = doPercentage(numberOfNewEntry, numberOfEntry);
            numberOfNewEntryBefore = 0;
        } else {
            System.out.println("pas de build precedente donc on positionne avec la valeur par défault : 0");
            numberOfRulesBefore = 0;
            numberOfPointeursBefore = 0;
            numberOfEntryBefore = 0;
            numberOfNewEntryBefore = 0;
            numberOfNewEntryPercent = 0;
            numberOfNewEntry = 0;
            newEntries = new ArrayList<ReportEntry>();
            numberOfNotFixedEntryBefore = 0;
            numberOfFixedEntryBefore = 0;
            topFiveEntriesBefore = new ArrayList<ReportEntry>();
            numberOfSeverityFormatageBefore = 0;
            numberOfSeverityPerformanceBefore = 0;
            numberOfSeverityDesignBefore = 0;
            numberOfSeverityLowSecurityBefore = 0;
            numberOfSeverityHighSecurityBefore = 0;
        }

        /**
         * We display few statistics
         */
        System.out.println("----------------------------------------------------");
        System.out.println("Nombre de rules dans le référentiel : " + numberOfRules);
        System.out.println("Nombre d'entry : " + numberOfEntry);
        System.out.println("Nombre de pointeurs : " + numberOfPointeurs);
        System.out.println("Nombre de pointeurs before : " + numberOfPointeursBefore);
        System.out.println("Nombre de fixed entry : " + numberOfFixedEntry);
        System.out.println("Nombre de not fixed entry : " + numberOfNotFixedEntry);
        System.out.println("numberOfSeverityFormatage  : " + numberOfSeverityFormatage);
        System.out.println("numberOfSeverityPerformance : " + numberOfSeverityPerformance);
        System.out.println("numberOfSeverityDesign : " + numberOfSeverityDesign);
        System.out.println("numberOfSeverityLowSecurity : " + numberOfSeverityLowSecurity);
        System.out.println("numberOfSeverityHighSecurity : " + numberOfSeverityHighSecurity);
        System.out.println("----------------------------------------------------");

        /** delta between this build and the one before */
        delta = numberOfEntry - numberOfEntryBefore;
        numberOfModules = project.getModules().size();
        System.out.println("End of SerenitecResult");
    }
    /**
     * Display the trend graph. Delegates to the the associated {@link ResultAction}.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in {@link ResultAction#doGraph(StaplerRequest, StaplerResponse, int)}
     */
    public void doErrorBySeverityTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {

        createGraph(request, response, "errorBySeverityTrend");
    }
    /**
     * Display the trend graph. Delegates to the the associated {@link ResultAction}.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in {@link ResultAction#doGraph(StaplerRequest, StaplerResponse, int)}
     */
    public void doPatternsBySeverityTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {

        createGraph(request, response, "patternsBySeverityTrend");
    }
    /**
     * Display the trend graph. Delegates to the the associated {@link ResultAction}.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in {@link ResultAction#doGraph(StaplerRequest, StaplerResponse, int)}
     */
    public void doTopFiveTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {

        createGraph(request, response, "topFiveTrend");
    }
    /**
     * Creates a trend graph or map.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in {@link ResultAction#doGraph(StaplerRequest, StaplerResponse, int)}
     */
    private void createGraph(final StaplerRequest request, final StaplerResponse response, final String type) throws IOException {

        final ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            action.doPersonalGraph(request, response, 200, type);
        }
    }
    /**
     * Returns the last valid result action.
     * 
     * @return the last valid result action, or <code>null</code> if no such action is found
     */
    public ResultAction<?> getLastAction() {

        final AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        ResultAction<?> resultat = null;
        if (lastBuild != null) {
            resultat = lastBuild.getAction(SerenitecResultAction.class);
        }
        return resultat;
    }
    /**
     * Returns the last finished build.
     * 
     * @return the last finished build or <code>null</code> if there is no such build
     */
    public AbstractBuild<?, ?> getLastFinishedBuild() {

        AbstractBuild<?, ?> lastBuild = owner;
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(SerenitecResultAction.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }

    public List<String> getModifiedFiles() {

        return modifiedFiles;
    }

    public EntriesContainer getContainer() {

        return getProject();
    }

    /**
     * Returns the serialization file.
     * 
     * @return the serialization file.
     */
    private XmlFile getDataFile() {

        return new XmlFile(XSTREAM, new File(getOwner().getRootDir(), "compiler-serenitec.xml"));
    }

    /**
     * @return the delta
     */
    public int getDelta() {

        return delta;
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     * 
     * @return the summary message
     */
    public String getDetails() {

        final String message = "Voisi le petit résumé enfin surtout le détail.. balbalbalbalblablablabla.";
        return message;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {

        return "Serenitec results";
    }
    /**
     * Returns the dynamic result of the warnings analysis (a detail page for a module, package or warnings file or a detail object for new
     * or fixed warnings).
     * 
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the warnings analysis (detail page for a package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {

        Object resultat = null;
        if (link.startsWith("module.")) {
            System.out.println("Analyse du module :  " + StringUtils.substringAfter(link, "module."));

            // return new TasksModuleDetail(getOwner(), getModule(StringUtils.substringAfter(link, "module.")), getDisplayName(), high,
            // normal, low);
        } else if (link.startsWith("Severity")) {
            System.out.println("Analyse de la severité  :  " + StringUtils.substringAfter(link, "Severity."));
            resultat = new SeverityDetails(getOwner(), save_project, Integer.parseInt(StringUtils.substringAfter(link, "Severity.")));
        } else if (link.startsWith("Errors")) {
            System.out.println("Analyse d'une erreur :  " + StringUtils.substringAfter(link, "Errors."));
            resultat = new ErrorDetails(getOwner(), save_project, StringUtils.substringAfter(link, "Errors."));
        } else if (link.startsWith("testedRules")) {
            resultat = new ProjectDetails(getOwner(), "testedRules", rules);
        } else if (link.startsWith("errors")) {
            resultat = new ProjectDetails(getOwner(), "errors", entries);
        } else if (link.startsWith("newErrors")) {
            resultat = new ProjectDetails(getOwner(), "newErrors", newEntries);
        } else if (link.startsWith("fixedErrors")) {
            resultat = new ProjectDetails(getOwner(), "fixedErrors", entriesfixed);
        } else if (link.startsWith("unfixedErrors")) {
            resultat = new ProjectDetails(getOwner(), "unfixedErrors", entriesnotfixed);
        }
        if (link.startsWith("patterns")) {
            resultat = new ProjectDetails(getOwner(), "patterns", pointeurs);
        } else if (link.startsWith("source.")) {
            System.out.println(StringUtils.substringAfter(link, "source."));
            System.out.println("******************************************" + "***************");
            System.out.println(save_project.getContainer().getPointeur(Integer.parseInt(StringUtils.substringAfter(link, "source."))));
            System.out.println("******************************************" + "***************");

            int key = Integer.parseInt(StringUtils.substringAfter(link, "source."));
            /**
             * We find the pattern having this ID key
             */
            ReportPointeur send_pattern = null;
            for (ReportPointeur pattern : pointeurs) {
                if (pattern.getKey() == key) {
                    send_pattern = pattern;
                }
            }
            resultat = new SourceDetail(getOwner(), send_pattern);
        }
        return resultat;
    }
    /**
     * @return the number of rules
     */
    public final int getNumberOfRules() {

        return numberOfRules;
    }
    /**
     * @return the number of rules before
     */
    public int getNumberOfRulesBefore() {

        return numberOfRulesBefore;
    }
    /**
     * @return the entries
     */
    public List<ReportEntry> getEntries() {

        return rules;
    }

    /**
     * @return the entriesfixed
     */
    public List<ReportEntry> getEntriesFixed() {

        return entriesfixed;
    }

    /**
     * @return the entriesnotfixed
     */
    public List<ReportEntry> getEntriesNotFixed() {

        return entriesnotfixed;
    }

    /**
     * @return the hightest severity entry discovered
     */
    public int getMaxSeverityDiscovered() {

        int resultat = 0;
        if (getNumberOfSeverityHighSecurity() > 0) {
            resultat = 5;
        } else if (getNumberOfSeverityLowSecurity() > 0) {
            resultat = 4;
        } else if (getNumberOfSeverityDesign() > 0) {
            resultat = 3;
        } else if (getNumberOfSeverityPerformance() > 0) {
            resultat = 2;
        } else if (getNumberOfSeverityFormatage() > 0) {
            resultat = 1;
        }
        return resultat;
    }

    public List<ReportEntry> getActiveEntry() {

        return entries;
    }

    /**
     * @return the newEntries
     */
    public List<ReportEntry> getNewEntries() {

        return newEntries;
    }

    /**
     * @return the numberMoyenneOfPointeursPerEntry
     */
    public float getNumberMoyenneOfPointeursPerEntry() {

        return numberMoyenneOfPointeursPerEntry;
    }

    /**
     * @return the numberOfEntry
     */
    public int getNumberOfEntry() {

        return numberOfEntry;
    }

    /**
     * @return the numberOfEntryBefore
     */
    public int getNumberOfEntryBefore() {

        return numberOfEntryBefore;
    }

    /**
     * Returns the module with the specified name.
     * 
     * @param name
     *            the module to get
     * @return the module
     */
    // public MavenModule getModule(final String name) {
    // MavenModule module;
    // if (emptyModules.containsKey(name)) {
    // module = emptyModules.get(name);
    // }
    // else {
    // module = getProject().getModule(name);
    // }
    // return module;
    // }
    /**
     * Returns the modules of this project.
     * 
     * @return the modules of this project
     */
    // public Collection<MavenModule> getModules() {
    // List<MavenModule> modules = new ArrayList<MavenModule>();
    // modules.addAll(emptyModules.values());
    // for (MavenModule module : getProject().getModules()) {
    // if (!emptyModules.containsKey(module.getName())) {
    // modules.add(module);
    // }
    // }
    // return modules;
    // }
    /**
     * @return the numberOfFixedEntry
     */
    public int getNumberOfFixedEntry() {

        return numberOfFixedEntry;
    }

    /**
     * @return the numberOfFixedEntryBefore
     */
    public int getNumberOfFixedEntryBefore() {

        return numberOfFixedEntryBefore;
    }

    /**
     * @return the numberOfFixedEntryPercent
     */
    public int getNumberOfFixedEntryPercent() {

        return (int) numberOfFixedEntryPercent;
    }

    /**
     * Returns the number of modules in this project.
     * 
     * @return the number of modules
     */
    public int getNumberOfModules() {

        return numberOfModules;
    }

    /**
     * @return the numberOfNewEntry
     */
    public int getNumberOfNewEntry() {

        return numberOfNewEntry;
    }

    /**
     * @return the numberOfNewEntryBefore
     */
    public int getNumberOfNewEntryBefore() {

        return numberOfNewEntryBefore;
    }

    /**
     * @return the numberOfNewEntryPercent
     */
    public int getNumberOfNewEntryPercent() {

        return (int) numberOfNewEntryPercent;
    }

    /**
     * @return the numberOfNotFixedEntry
     */
    public int getNumberOfNotFixedEntry() {

        return numberOfNotFixedEntry;
    }

    /**
     * @return the numberOfNotFixedEntryBefore
     */
    public int getNumberOfNotFixedEntryBefore() {

        return numberOfNotFixedEntryBefore;
    }

    /**
     * @return the numberOfNotFixedEntryPercent
     */
    public int getNumberOfNotFixedEntryPercent() {

        return (int) numberOfNotFixedEntryPercent;
    }

    /**
     * @return the numberOfPointeurs
     */
    public int getNumberOfPointeurs() {

        return numberOfPointeurs;
    }

    /**
     * @return the numberOfPointeursBefore
     */
    public int getNumberOfPointeursBefore() {

        return numberOfPointeursBefore;
    }

    /**
     * @return the numberOfSeverityDesign
     */
    public int getNumberOfSeverityDesign() {

        return numberOfSeverityDesign;
    }

    /**
     * @return the numberOfSeverityDesignBefore
     */
    public int getNumberOfSeverityDesignBefore() {

        return numberOfSeverityDesignBefore;
    }

    /**
     * @return the numberOfSeverityDesignPercent
     */
    public int getNumberOfSeverityDesignPercent() {

        return (int) numberOfSeverityDesignPercent;
    }

    /**
     * @return the numberOfSeverityFormatage
     */
    public int getNumberOfSeverityFormatage() {

        return numberOfSeverityFormatage;
    }

    /**
     * @return the numberOfSeverityFormatageBefore
     */
    public int getNumberOfSeverityFormatageBefore() {

        return numberOfSeverityFormatageBefore;
    }

    /**
     * @return the numberOfSeverityFormatagePercent
     */
    public int getNumberOfSeverityFormatagePercent() {

        return (int) numberOfSeverityFormatagePercent;
    }

    /**
     * @return the numberOfSeverityHighSecurity
     */
    public int getNumberOfSeverityHighSecurity() {

        return numberOfSeverityHighSecurity;
    }

    /**
     * @return the numberOfSeverityHighSecurityBefore
     */
    public int getNumberOfSeverityHighSecurityBefore() {

        return numberOfSeverityHighSecurityBefore;
    }

    /**
     * @return the numberOfSeverityHighSecurityPercent
     */
    public int getNumberOfSeverityHighSecurityPercent() {

        return (int) numberOfSeverityHighSecurityPercent;
    }

    /**
     * @return the numberOfSeverityLowSecurity
     */
    public int getNumberOfSeverityLowSecurity() {

        return numberOfSeverityLowSecurity;
    }

    /**
     * @return the numberOfSeverityLowSecurityBefore
     */
    public int getNumberOfSeverityLowSecurityBefore() {

        return numberOfSeverityLowSecurityBefore;
    }

    /**
     * @return the numberOfSeverityLowSecurityPercent
     */
    public int getNumberOfSeverityLowSecurityPercent() {

        return (int) numberOfSeverityLowSecurityPercent;
    }

    /**
     * @return the numberOfSeverityPerformance
     */
    public int getNumberOfSeverityPerformance() {

        return numberOfSeverityPerformance;
    }

    /**
     * @return the numberOfSeverityPerformanceBefore
     */
    public int getNumberOfSeverityPerformanceBefore() {

        return numberOfSeverityPerformanceBefore;
    }

    /**
     * @return the numberOfSeverityPerformancePercent
     */
    public int getNumberOfSeverityPerformancePercent() {

        return (int) numberOfSeverityPerformancePercent;
    }

    /**
     * Returns the build as owner of this action.
     * 
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {

        return owner;
    }

    /**
     * Returns the number of warnings of the specified package in the previous build.
     * 
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getPreviousNumberOfWarnings(final String packageName) {

        final Project previousResult = getPreviousResult();
        int resultat = 0;
        if (previousResult != null) {
            resultat = previousResult.getPackage(packageName).getNumberOfEntry();
        }
        return resultat;
    }

    /**
     * Returns the results of the previous build.
     * 
     * @return the result of the previous build, or <code>null</code> if no such build exists
     */
    public Project getPreviousResult() {

        final SerenitecResultAction action = getOwner().getAction(SerenitecResultAction.class);
        Project resultat = null;
        if (action.hasPreviousResultAction()) {
            resultat = action.getPreviousResultAction().getResult().getProject();
        }
        return resultat;
    }

    /**
     * Returns the associated project of this result.
     * 
     * @return the associated project of this result.
     */
    public Project getProject() {

        return save_project;
    }

    public Project getProjectSerenitec() {

        return projectSerenitec.get();
    }

    /**
     * @return the topFiveEntries
     */
    public List<ReportEntry> getTopFiveEntries() {

        return topFiveEntries;
    }

    /**
     * @return the topFiveEntriesBefore
     */
    public List<ReportEntry> getTopFiveEntriesBefore() {

        return topFiveEntriesBefore;
    }

    /**
     * @return if it has rules
     */
    public boolean hasRules() {

        return rules.size() > 0;
    }
    /**
     * @return if it has entries
     */
    public boolean hasEntries() {

        return entries.size() > 0;
    }

    /**
     * Returns whether a module with an error is part of this project.
     * 
     * @return <code>true</code> if at least one module has an error.
     */
    public boolean hasError() {

        return !errors.isEmpty();
    }

    /**
     * Returns whether a previous build result exists.
     * 
     * @return <code>true</code> if a previous build result exists.
     */
    public boolean hasPreviousResult() {

        return getOwner().getAction(SerenitecResultAction.class).hasPreviousResultAction();
    }

    /**
     * Returns whether this result belongs to the last build.
     * 
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {

        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Loads the warnings results and the result of the previous build and wraps them in a weak reference that might get removed by the
     * garbage collector.
     */
    @java.lang.SuppressWarnings("unchecked")
    private void loadPreviousResult() {

        // loadResult();
        //
        // if (hasPreviousResult()) {
        // newWarnings = new WeakReference<Set<FileAnnotation>>(
        // AnnotationDifferencer.getNewWarnings(getProject().getAnnotations(), getPreviousResult().getAnnotations()));
        // }
        // else {
        // newWarnings = new WeakReference<Set<FileAnnotation>>(new HashSet<FileAnnotation>(getProject().getAnnotations()));
        // }
        // if (hasPreviousResult()) {
        // fixedWarnings = new WeakReference<Set<FileAnnotation>>(
        // AnnotationDifferencer.getFixedWarnings(getProject().getAnnotations(), getPreviousResult().getAnnotations()));
        // }
        // else {
        // fixedWarnings = new WeakReference<Set<FileAnnotation>>(Collections.EMPTY_SET);
        // }
    }

    /**
     * Loads the warnings results and wraps them in a weak reference that might get removed by the garbage collector.
     */
    private void loadResult() {

    }

    public List<ReportEntry> getRules() {

        return rules;
    }
    public int getNumberOfSeverityDesignPatterns() {

        return numberOfSeverityDesignPatterns;
    }
    public int getNumberOfSeverityFormatagePatterns() {

        return numberOfSeverityFormatagePatterns;
    }
    public int getNumberOfSeverityHighSecurityPatterns() {

        return numberOfSeverityHighSecurityPatterns;
    }
    public int getNumberOfSeverityLowSecurityPatterns() {

        return numberOfSeverityLowSecurityPatterns;
    }
    public int getNumberOfSeverityPerformancePatterns() {

        return numberOfSeverityPerformancePatterns;
    }
}
