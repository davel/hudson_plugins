/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.2 $
 * @since $Date: 2008/07/24 09:44:14 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;

import hudson.plugins.serenitec.SerenitecResultAction;
import hudson.plugins.serenitec.parseur.ReportEntry;
import hudson.plugins.serenitec.parseur.ReportPointeur;
import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.AbstractProject;
import java.util.List;


/**
 * Renders details of the project
 * specific line number.
 *
 * @author Georges Bossert
 */
public class ProjectDetails implements ModelObject
{
    /**
     * Type of the details
     */
    private final String _details;
    /**
     * Define the displayName in function of the type of details
     */
    private String titre = "";
    /**
     * Asbtract Build
     */
    private final AbstractBuild<?, ?> _owner;
    /**
     * Abstract Project
     */
    private final AbstractProject<?, ?> _project;
    /**
     * List des regles pour notre repository
     */ 
    private List<?> Data;

    public List<?> getData()
    {
        return Data;
    }
    private boolean isEntry;

    public void setData(List<?> data)
    {
        this.Data = data;
    }
    
    public ProjectDetails(final AbstractBuild<?, ?> owner, String details, List<?> data)
    {
        this._details = details;
        this._owner = owner;
        this._project = owner.getProject();
        this.isEntry = true;
        
        /**
         * Generates _displayName in function of the details
         */
        setDisplayName();
        /**
         * Get datas
         */
        if (_details.equals("testedRules"))
        {
            Data = ( List<ReportEntry> ) data;
        }
        else if (_details.equals("errors"))
        {
            Data = ( List<ReportEntry> ) data;
        }
        else if (_details.equals("newErrors"))
        {
            Data = ( List<ReportEntry> ) data;
        }
        else if (_details.equals("fixedErrors"))
        {
            Data = ( List<ReportEntry> ) data;
        }
        else if (_details.equals("unfixedErrors"))
        {
            Data = ( List<ReportEntry> ) data;
        }
        else if (_details.equals("patterns"))
        {
            Data = ( List<ReportPointeur> ) data;
            this.isEntry = false;
        }
    }

    public boolean isEntry()
    {
        return isEntry;
    }

   
   
     /**
     * Creates a trend graph or map.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link ResultAction#doGraph(StaplerRequest, StaplerResponse, int)}
     */
    private void createGraph(final StaplerRequest request, final StaplerResponse response) throws IOException
    {
        System.out.println("execution de ProjectDetail.createGraph()");
        final AbstractResultAction<?> action = ( AbstractResultAction<?> ) getLastAction();
        if (action == null) 
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else 
        {
            action.doDetailsGraph(request, response, _details);
        }
    }
    public void doTrend(final StaplerRequest request,
            final StaplerResponse response) throws IOException
    {
        System.out.println("execution de ProjectDetail.doTrend()");
        createGraph(request, response);
    }
    /**
     * Returns the last valid result action.
     * 
     * @return the last valid result action, or <code>null</code> if no such
     *         action is found
     */
    public ResultAction<?> getLastAction()
    {
        final AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        if (lastBuild != null)
        {
            return lastBuild.getAction(SerenitecResultAction.class);
        }
        return null;
    }
    /**
     * Returns the last finished build.
     * 
     * @return the last finished build or <code>null</code> if there is no
     *         such build
     */
    public AbstractBuild<?, ?> getLastFinishedBuild()
    {
        AbstractBuild<?, ?> lastBuild = _project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding()
                || lastBuild.getAction(SerenitecResultAction.class) == null))
        {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }
    
    
    
    /**
     * Getter for owner
     * @return the AbstractBuild
     */
    public AbstractBuild<?, ?> getOwner()
    {
        return _owner;
    }
    /**
     * Title of the page in the navigation anchor bar
     * @return the title
     */
    public String getDisplayName()
    {
        return titre;
    }
    /**
     * Define the title in function of the type of detail
     */
    private void setDisplayName()
    {
        if (_details.startsWith("testedRules"))
        {
            titre = "Tested Rules trend";
        }
    }

}
