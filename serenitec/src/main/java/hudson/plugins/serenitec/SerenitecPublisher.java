/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.8 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;


import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.serenitec.parseur.Gettingxml;
import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.plugins.serenitec.parseur.ReportFile;
import hudson.plugins.serenitec.util.HealthAwarePublisher;
import hudson.plugins.serenitec.util.HealthReportBuilder;
import hudson.plugins.serenitec.util.Project;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The class SerenitecPublisher declares the methods SerenitecPublisher
 * 
 * @author $Author: georges $
 * @version $Revision: 1.8 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Silicomp AQL 2007-2008
 */
public class SerenitecPublisher extends HealthAwarePublisher
{

    /*
     * Descriptor de notre plugin
     */
    public static final SerenitecDescriptor SERENITEC_DESCRIPTOR = new SerenitecDescriptor();

    /**
     * Creates a new instance of <code>WarningPublisher</code>.
     * 
     * @param threshold
     *            Entries threshold to be reached if a build should be considered as unstable.
     * @param healthy
     *            Report health as 100% when the number of entries is less than this value
     * @param unHealthy
     *            Report health as 0% when the number of entries is greater than this value
     * @param height
     *            the height of the trend graph
     */
    @DataBoundConstructor
    public SerenitecPublisher(final String threshold, final String healthy, final String unHealthy, final String height) {

        super(threshold, healthy, unHealthy, height, "Serenitec Hudson Plugin");
    }

    @Override
    protected boolean canContinue(final Result result) {

        return result != Result.ABORTED;
    }

    /*
     * @see hudson.model.Describable#getDescriptor()
     */
    public Descriptor<Publisher> getDescriptor() {

        return SERENITEC_DESCRIPTOR;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {

        return new SerenitecProjectAction(project, getTrendHeight());
    }

    @Override
    protected Project perform(final AbstractBuild<?, ?> build, final PrintStream logger) throws InterruptedException, IOException {

        /**
         * Creating the project
         */
        Project projet = new Project();

        /**
         * Scanning for the report
         */

        String uri_report = build.getRootDir().getParentFile().getParentFile().getAbsolutePath() + "\\" + "serenitec-report.xml";
        log(logger, "Scanning for the report in " + uri_report);
        final File report = new File(uri_report);
        if (!report.exists() || !report.canRead()) {
            log(logger, "Error : Unable to read xml event report.");
            build.setResult(Result.FAILURE);
        } else {
            /**
             * We parse the report
             */
            log(logger, "Opening report file and parsing results :");
            final Gettingxml parseur = new Gettingxml(report.getAbsolutePath());
            try {
                parseur.parse();
            } catch (final Exception e) {
                log(logger, "Error on parsing results : " + e.getLocalizedMessage());
                build.setResult(Result.FAILURE);
            }
            ArrayList<ReportEntry> xml = parseur.result();
            ArrayList<ReportFile> xml_fichier = parseur.resultFiles();
            projet.addEntries(xml, build);
            System.out.println("Appel de projet.addFiles avec " + xml_fichier.size() + "fichiers.");
            projet.addFiles(xml_fichier);
            log(logger, "-Repository-------------------------------------");
            log(logger, " Number of Rules : " + projet.getNumberOfRules());
            log(logger, " Number of Errors : " + projet.getNumberOfEntry());
            log(logger, " Number of Patterns : " + projet.getNumberOfPointeurs());
            log(logger, " Number of Fixed errors : " + projet.getNumberOfFixedEntry());
            log(logger, " Number of Unfixed errors : " + projet.getNumberOfNotFixedEntry());
            log(logger, " Number of Scanned files : " + projet.getNumberOfFiles());
            log(logger, "------------------------------------------------");
            /**
             * Implementing the Result Builder
             */
            final SerenitecResult resultat = new SerenitecResultBuilder().build(build, projet);
            /**
             * Implementing the HealthReportBuilder
             */
            final HealthReportBuilder healthReportBuilder = createHealthReporter("Serenitec Reports : 1 open task found.",
                    "Serenitec Reports : {\"%d\"} open tasks found.");
            log(logger, "Adding new Serenitec Result Action into the build");
            build.getActions().add(new SerenitecResultAction(build, healthReportBuilder, resultat));
        }
        return projet;
    }
}
