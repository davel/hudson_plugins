/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.4 $
 * @since $Date: 2008/07/23 12:05:04 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util.model;


import hudson.plugins.serenitec.parseur.ReportEntry;

import java.util.List;

/**
 * Provides an entry counter for a model object.
 */
public interface EntriesProvider
{

    /**
     * @return rules
     */
    public List<ReportEntry> getRules();

    /**
     * return entries
     */
    public List<ReportEntry> getEntries();

    /**
     * @return the entriesfixed
     */
    public List<ReportEntry> getEntriesFixed();

    /**
     * @return the entriesnotfixed
     */
    public List<ReportEntry> getEntriesNotFixed();

    /**
     * @return the highest severity entry discovered
     */
    public int getMaxSeverityDiscovered();

    /**
     * @return the numberOfRules
     */
    public int getNumberOfRules();

    /**
     * @return the numberOfEntry
     */
    public int getNumberOfEntry();
    /**
     * @return the number of NewEntry;
     */
    public int getNumberOfNewEntry();

    /**
     * @return the numberOfFixedEntry
     */
    public int getNumberOfFixedEntry();

    /**
     * @return the numberOfNotFixedEntry
     */
    public int getNumberOfNotFixedEntry();

    /**
     * @return the numberOfPointeurs
     */
    public int getNumberOfPointeurs();

    /**
     * @return the numberOfSeverityDesign
     */
    public int getNumberOfSeverityDesign();

    /**
     * @return the numberOfSeverityDesignPatterns
     */
    public int getNumberOfSeverityDesignPatterns();

    /**
     * @return the numberOfSeverityFormatage
     */
    public int getNumberOfSeverityFormatage();

    /**
     * @return the numberOfSeverityFormatagePatterns
     */
    public int getNumberOfSeverityFormatagePatterns();

    /**
     * @return the numberOfSeverityHighSecurity
     */
    public int getNumberOfSeverityHighSecurity();

    /**
     * @return the numberOfSeverityHighSecurityPatterns
     */
    public int getNumberOfSeverityHighSecurityPatterns();

    /**
     * @return the numberOfSeverityLowSecurity
     */
    public int getNumberOfSeverityLowSecurity();

    /**
     * @return the numberOfSeverityLowSecurityPatterns
     */
    public int getNumberOfSeverityLowSecurityPatterns();

    /**
     * @return the numberOfSeverityPerformance
     */
    public int getNumberOfSeverityPerformance();

    /**
     * @return the numberOfSeverityPerformancePatterns
     */
    public int getNumberOfSeverityPerformancePatterns();

    /**
     * @return the topFiveEntries
     */
    public List<ReportEntry> getTopFiveEntries();

    /**
     * @return the number of files
     */
    public int getNumberOfFiles();

    /**
     * @return the number of files with errors
     */
    public int getNumberOfFilesWithErrors();

    /**
     * @return the number of files with no errors
     */
    public int getNumberOfFilesWithNoErrors();
}
