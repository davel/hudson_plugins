/**
 * Hudson Serenitec plugin
 * 
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.8 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;

import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;

/**
 * SerenitecBuilder.
 */
public class SerenitecBuilder extends Builder 
{
    /**
     * Serenitec descriptor.
     */
    public static final SerenitecDescriptorBuilder SERENITEC_DESCRIPTOR 
        = new SerenitecDescriptorBuilder();
    /**
     * Contructor.
     */
    public SerenitecBuilder()
    {
        super();
    }
    /**
     * get Descriptor
     * @return Serenitec descriptor
     */
    public Descriptor < Builder > getDescriptor() 
    {
        return SERENITEC_DESCRIPTOR;
    }
    /** {@inheritDoc} */
    @Override
    public boolean prebuild(final Build build, final BuildListener listener) 
    {
        //build.setResult(Result.FAILURE);
        return false;
    }
}
