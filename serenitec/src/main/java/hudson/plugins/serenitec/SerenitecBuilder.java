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
import hudson.model.BuildListener;
import hudson.tasks.Builder;

/**
 * SerenitecBuilder.
 */
public class SerenitecBuilder extends Builder 
{
    /**
     * Contructor.
     */
    public SerenitecBuilder()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public boolean prebuild(final AbstractBuild<?,?> build, final BuildListener listener)
    {
        //build.setResult(Result.FAILURE);
        return false;
    }
}
