/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;


import hudson.plugins.serenitec.util.AbstractResultAction;
import hudson.plugins.serenitec.util.HealthReportBuilder;
import hudson.model.AbstractBuild;


import java.util.NoSuchElementException;

/**
 * Controls the live cycle of the warnings results.
 * This action persists the results of the warnings analysis
 * of a build and displays the
 * results on the build page.
 * The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the warnings result trend.
 * </p>
 * 
 * @author Ulli Hafner
 */
public class SerenitecResultAction extends AbstractResultAction<SerenitecResult>
{
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     * 
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     */
    public SerenitecResultAction(final AbstractBuild<?, ?> owner,
            final HealthReportBuilder healthReportBuilder)
    {
        super(owner, healthReportBuilder);
    }

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     * 
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     * @param result
     *            the result in this build
     */
    public SerenitecResultAction(final AbstractBuild<?, ?> owner,
            final HealthReportBuilder healthReportBuilder,
            final SerenitecResult result)
    {
        super(owner, healthReportBuilder, result);
    }
    /** {@inheritDoc} */
    @Override
    protected SerenitecDescriptor getDescriptor()
    {
        return SerenitecPublisher.SERENITEC_DESCRIPTOR;
    }

    /** {@inheritDoc} */
    public String getDisplayName()
    {
        return "Serenitec Project Action Name";
    }

    /** {@inheritDoc} */
    public String getMultipleItemsTooltip(final int numberOfItems)
    {
        return "Nombre d'events détecté : " + numberOfItems;
    }

    /**
     * Gets the warnings result of the previous build.
     * 
     * @return the warnings result of the previous build.
     * @throws NoSuchElementException
     *             if there is no previous build for this action
     */
    public SerenitecResultAction getPreviousResultAction()
    {
        final AbstractResultAction<SerenitecResult> previousBuild =
                getPreviousBuild();
        if (previousBuild instanceof SerenitecResultAction)
        {
            return (SerenitecResultAction) previousBuild;
        }
        throw new NoSuchElementException(
                "There is no previous build for action " + this);
    }

    /** {@inheritDoc} */
    public String getSingleItemTooltip()
    {
        return "Le seul event détecté.";
    }
}
