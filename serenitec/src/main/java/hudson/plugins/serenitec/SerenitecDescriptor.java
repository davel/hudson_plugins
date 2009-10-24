/**
 * Hudson Serenitec plugin.
 * 
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.4 $
 * @since $Date: 2008/07/16 14:52:22 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.plugins.serenitec.util.ThresholdValidator;

/**
 * The class SerenitecDescriptor declares the methods in charge of the maven
 * plugin.
 *
 * @author $Author: georges $
 * @version $Revision: 1.4 $
 * @since $Date: 2008/07/16 14:52:22 ${date}
 * @copyright Silicomp AQL 2007-2008
 * @goal refactor
 * @phase process-sources
 */
public class SerenitecDescriptor extends BuildStepDescriptor < Publisher > 
{
    /** Icons to use. */
    private static final String ACTION_ICON =
            "/plugin/serenitec/icons/warnings-24x24.png";
    /** Plugin name. */
    private static final String PLUGIN_NAME = "Serenitec";

    /**
     * Constructor.
     */
    public SerenitecDescriptor() 
    {
        super(SerenitecPublisher.class);
    }
    /**
     * Check Patterns.
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
    /** {@inheritDoc} */
    @Override
    public String getDisplayName() 
    {
        return "Publish Serenitec Refactor";
    }
    /** {@inheritDoc} */
    @Override
    public final String getHelpFile() 
    {
        return "/plugin/" + getPluginName() + "/help.html";
    }
    /**
     * Getter for Action icon
     * @return Action Icon
     */
    public String getIconUrl() 
    {
        return ACTION_ICON;
    }
    /**
     * Getter of the Plugin Name.
     * @return Plugin name
     */
    public String getPluginName() 
    {
        return PLUGIN_NAME;
    }
    /**
     * Getter for results url
     * @return path to the results
     */
    public final String getPluginResultUrlName() 
    {
        return getPluginName() + "Result";
    }
    /** {@inheritDoc} */
    @Override
    public boolean isApplicable(final
            Class<? extends AbstractProject> jobType) 
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public SerenitecPublisher newInstance(final StaplerRequest request,
            final JSONObject formData) throws FormException 
    {
        return request.bindJSON(SerenitecPublisher.class, formData);
    }

}
