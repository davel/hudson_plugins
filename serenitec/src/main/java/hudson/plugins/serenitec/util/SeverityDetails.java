package hudson.plugins.serenitec.util;


import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.serenitec.parseur.ReportEntry;

import java.util.List;

public class SeverityDetails implements ModelObject
{

    private final String              titre;
    private final int                 severity;
    private final List<ReportEntry>   data;
    /**
     * Asbtract Build
     */
    private final AbstractBuild<?, ?> owner;
    /**
     * Abstract Project
     */
    private final Project             project;

    /**
     * Generates DATA for Severity Details
     * 
     * @param Owner
     * @param Titre
     * @param Severity
     */
    public SeverityDetails(final AbstractBuild<?, ?> Owner, Project Projet, int Severity) {

        this.titre = "Errors for Severity : " + defineTitle(Severity);
        this.owner = Owner;
        this.project = Projet;
        this.severity = Severity;

        /**
         * Generate datas
         */
        this.data = project.getEntriesBySeverity().get(severity);
    }
    private String defineTitle(int severity2) {

        String resultat = null;
        if (severity2 == 1) {
            resultat = "Formating";
        } else if (severity2 == 2) {
            resultat = "Language evolution";
        } else if (severity2 == 3) {
            resultat = "Design";
        } else if (severity2 == 4) {
            resultat = "Low security";
        } else if (severity2 == 5) {
            resultat = "High security";
        }
        return resultat;

    }
    /**
     * Getter for owner
     * 
     * @return the AbstractBuild
     */
    public AbstractBuild<?, ?> getOwner() {

        return owner;
    }
    /**
     * Return the title of the page
     */
    public String getDisplayName() {

        return titre;
    }

    public List<ReportEntry> getData() {

        return data;
    }

}
