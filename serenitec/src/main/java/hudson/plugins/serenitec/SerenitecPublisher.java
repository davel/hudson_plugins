/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.8 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;

import hudson.plugins.serenitec.parseur.Gettingxml;
import hudson.plugins.serenitec.parseur.ReportDescription;
import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.plugins.serenitec.parseur.ReportPointeur;
import hudson.plugins.serenitec.util.HealthAwarePublisher;
import hudson.plugins.serenitec.util.HealthReportBuilder;
import hudson.plugins.serenitec.util.Project;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.List;
import org.apache.commons.io.filefilter.FileFileFilter;
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
    public static final SerenitecDescriptor SERENITEC_DESCRIPTOR =
            new SerenitecDescriptor();

    /**
     * Creates a new instance of <code>WarningPublisher</code>.
     * 
     * @param threshold
     *            Entries threshold to be reached if a build should be
     *            considered as unstable.
     * @param healthy
     *            Report health as 100% when the number of entries is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of entries is greater than
     *            this value
     * @param height
     *            the height of the trend graph
     */
    @DataBoundConstructor
    public SerenitecPublisher(final String threshold, final String healthy,
            final String unHealthy, final String height)
    {
        super(threshold, healthy, unHealthy, height, "SERENITEC");
    }

    @Override
    protected boolean canContinue(final Result result)
    {

        return result != Result.ABORTED;
    }

    /*
     * @see hudson.model.Describable#getDescriptor()
     */
    public Descriptor<Publisher> getDescriptor()
    {

        return SERENITEC_DESCRIPTOR;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project)
    {
        return new SerenitecProjectAction(project, getTrendHeight());
    }

    @Override
    protected Project perform(final AbstractBuild<?, ?> build,
            final PrintStream logger) throws InterruptedException, IOException
    {
        /**
         * Detecting if it's a multi module project
         */
        log(logger, "Detecting reports fils :");
        log(logger, "build.getRootDir() : " + build.getRootDir());
        
        
        
        
        
        
        
        
        
        /**
         * If we have more then one module we concat reports
         */
        log(logger, "Concatenation of reports :");
        
        /**
         * We open the report
         */
        log(logger, "Opening global report file :");
        
        /**
         * We parse the report
         */
        log(logger, "Parsing results :");
        
        
        log(logger, "Opening report file ...");

        // we search for the xml
        final String url_fichier_xml = build.getRootDir().getParentFile()
                .getParentFile().toString() + "/workspace/serenitec-report.xml";

        final File report_xml = new File(url_fichier_xml);

        final Project projet = new Project();
        
        // we verify that the file exist
        if (!report_xml.canRead())
        {
            log(logger, "Unable to read xml event report.");
            build.setResult(Result.FAILURE);
        }
        else
        {
            log(logger, "Analysing Serenitec report");
            final Gettingxml parseur = new Gettingxml(url_fichier_xml);
            ArrayList<ReportEntry> xml;
            try
            {
                parseur.parse();
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            xml = parseur.result();
            
            projet.addEntries(xml);
            log(logger, "----------------------------------------------------" +
                    "------------");
            log(logger, "Number of events : " + projet.getNumberOfEntry());
            log(logger, "----------------------------------------------------" +
                    "------------");
            for (final ReportEntry entry : projet.getEntries())
            {
                log(logger, entry.getName());
                log(logger, "Severity : " +
                        Integer.toString(entry.getSeverity()));
                for (final ReportDescription description :
                    entry.getDescriptions())
                {
                    log(logger, description.getLanguage() 
                            + " : " + description.getDescription()
                            + " (" + description.getHelpreference() + ")");
                }
                for (final ReportPointeur pointeur : entry.getPointeurs())
                {
                    log(logger, pointeur.getFullpath() + " : "
                            + pointeur.getFilename()
                            + " (" + pointeur.getLinenumber() + ") "
                            + pointeur.isIsfixed());
                }
                log(logger, "------------------------------------------------" +
                        "-----------------");
            }
            for (final ReportEntry entry : projet.getEntriesNotFixed())
            {
                log(logger, "Not Fixed : " + entry.getName() + " "
                        + entry.getSeverity());
            }
            log(logger, "Implementing Serenitec Result Builder.");
            SerenitecResultBuilder test = new SerenitecResultBuilder();
            log(logger, "build()");
            log(logger, "Nombre d'entry : "+test.build(build, projet).getNumberOfEntry());
            
            final SerenitecResult resultat = new SerenitecResultBuilder().build(build, projet);
            
            
            
            
            System.out.println("Test ...");
            log(logger, "Implementing Health Report Builder.");
            final HealthReportBuilder healthReportBuilder =
                    createHealthReporter("Messages.Warnings_ResultAction_" +
                    "HealthReportSingleItem()",
                    "Messages.Warnings_ResultAction_" +
                    "HealthReportMultipleItem(\"%d\")");
            log(logger, "Adding new Serenitec Result Action into the build");
            build.getActions().add(new SerenitecResultAction(
                    build, healthReportBuilder, resultat));
        }
        System.out.println("Resultat du retour : ");
        System.out.println(projet.getNumberOfEntry());
        return projet;
    }
}
