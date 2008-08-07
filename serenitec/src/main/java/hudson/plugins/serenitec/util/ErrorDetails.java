package hudson.plugins.serenitec.util;


import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.serenitec.SerenitecResultAction;
import hudson.plugins.serenitec.parseur.ReportEntry;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
    /**
     * Getter for owner
     * 
     * @return the AbstractBuild
     */
    public AbstractBuild<?, ?> getOwner() {

        return owner;
    }

    public String getDisplayName() {

        return "Error " + entry.getName();
    }

    public ReportEntry getEntry() {

        return entry;
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
    public void doRepartitionPie(final StaplerRequest request, final StaplerResponse response) throws IOException {

        /**
         * We calculate the 2 numbers
         */
        int n1 = entry.getNumberOfPointeurs();
        int n2 = project.getNumberOfPointeurs();

        createGraph(request, response, n1, n2);
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
    private void createGraph(final StaplerRequest request, final StaplerResponse response, final int n1, final int n2) throws IOException {

        final ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            action.doRulesRepartitionPie(request, response, 200, n1, n2, error);
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

}
