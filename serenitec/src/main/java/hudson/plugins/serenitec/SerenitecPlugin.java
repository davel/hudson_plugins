/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/16 14:52:22 ${date}
 * @copyright Université de Rennes 1
 */

package hudson.plugins.serenitec;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Registers the warnings plug-in publisher and reporter.
 *
 * @author Ulli Hafner
 */
public class SerenitecPlugin extends Plugin
{
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD")
    public void start() throws Exception
    {
        /*
         * on ajoute un "Builder" dans la liste. Builder : réaliser un
         * pre-build
         */
        //BuildStep.BUILDERS.add(SerenitecBuilder.SERENITEC_DESCRIPTOR);
        /*
         * on ajoute un "Publisher" dans la liste. Publisher : réaliser un
         * post-action sur les résultats de compilation
         */
        BuildStep.PUBLISHERS.addRecorder(
                SerenitecPublisher.SERENITEC_DESCRIPTOR);
    }
}
