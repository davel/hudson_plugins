package hudson.plugins.serenitec.util;


import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.serenitec.parseur.ReportEntry;

public class ErrorDetails implements ModelObject
{

    private final String              titre;
    private final String              error;
    /**
     * Asbtract Build
     */
    private final AbstractBuild<?, ?> owner;
    /**
     * Abstract Project
     */
    private final Project             project;
    private ReportEntry               entry;

    public ErrorDetails(final AbstractBuild<?, ?> Owner, Project Projet, String error) {

        this.titre = "Error " + error;
        this.owner = Owner;
        this.project = Projet;
        this.error = error;
        this.entry = null;
        /**
         * Find out this rules from the project
         */
        for (ReportEntry test_entry : Projet.getEntries()) {
            if (test_entry.getName().equals(error)) {
                entry = test_entry;
            }
        }

    }

    public String getDisplayName() {

        return "Error " + entry.getName();
    }

    public ReportEntry getEntry() {

        return entry;
    }

}
