/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 14:52:22 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.plugins.serenitec.util.ThresholdValidator;
import hudson.tasks.Builder;

/**
 * The class SerenitecDescriptor declares the methods in charge of the maven
 * plugin.
 *
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 14:52:22 ${date}
 * @goal refactor
 * @phase process-sources
 */
@Extension
public class SerenitecDescriptorBuilder
        extends BuildStepDescriptor < Builder > 
{
    /**
     * Icons to use.
     */
    private static final String ACTION_ICON =
            "/plugin/serenitec/icons/warnings-24x24.png";
    /**
     * Plugin Name.
     */
    private static final String PLUGIN_NAME = "Serenitec";

    /**
     * Constructor.
     */
    public SerenitecDescriptorBuilder() 
    {
        super(SerenitecBuilder.class);
    }
    /**
     * Check Patterns
     * @throws java.io.IOException
     */
    public final FormValidation doCheckPattern(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException
    {
        return FilePath.validateFileMask(project.getSomeWorkspace(),value);
    }
    /**
     * Check Threshold
     */
    public final FormValidation doCheckThreshold(@QueryParameter String value)
    {
        return ThresholdValidator.check(value);
    }

    @Override
    public String getDisplayName() 
    {
        return "Publish Serenitec Builder Refactor";
    }

    @Override
    public final String getHelpFile()
    {
        return "/plugin/" + getPluginName() + "/help.html";
    }

    public String getIconUrl()
    {
        return ACTION_ICON;
    }

    public String getPluginName()
    {
        return PLUGIN_NAME;
    }

    public final String getPluginResultUrlName()
    {
        return getPluginName() + "Result";
    }

    @Override
    public boolean isApplicable(
            final Class <? extends AbstractProject > jobType)
    {
        return true;
    }

    @Override
    public SerenitecBuilder newInstance(final StaplerRequest request,
        final JSONObject formData) throws FormException
    {
        return request.bindJSON(SerenitecBuilder.class, formData);
    }
}
