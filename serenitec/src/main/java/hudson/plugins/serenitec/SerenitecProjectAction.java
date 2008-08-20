/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec;


import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.plugins.serenitec.util.ResultAction;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * The class SerenitecProjectAction.
 * 
 * @version $Revision: 1.5 $
 * @since $Date: 2008/07/24 09:44:13 ${date}
 */
public class SerenitecProjectAction implements Action
{

    /**
     * SERIAL UID
     */
    private static final long           serialVersionUID = -2512038161663973656L;
    /** One year (in seconds). */
    private static final int            ONE_YEAR         = 60 * 60 * 24 * 365;
    /** Determines the height of the trend graph. */
    private final int                   height;
    /**
     * The icon URL of this action: it will be shown as soon as a result is available.
     */
    private final String                iconUrl;
    /** Project that owns this action. */
    @SuppressWarnings("Se")
    private final AbstractProject<?, ?> project;
    /** Plug-in results URL. */
    private final String                resultUrl;
    /** Plug-in URL. */
    private final String                url;

    /**
     * Instantiates a new find bugs project action.
     * 
     * @param project
     *            the project that owns this action
     * @param height
     *            the height of the trend graph
     */
    public SerenitecProjectAction(final AbstractProject<?, ?> project, final int height) {

        this(project, SerenitecPublisher.SERENITEC_DESCRIPTOR, height);
    }

    /**
     * Creates a new instance of <code>SerenitecProjectAction</code>.
     * 
     * @param project
     *            the project that owns this action
     * @param resultActionType
     *            the type of the result action
     * @param plugin
     *            the plug-in that owns this action
     * @param height
     *            the height of the trend graph
     */
    public SerenitecProjectAction(final AbstractProject<?, ?> project, final SerenitecDescriptor plugin, final int height) {

        this.project = project;
        this.height = height;
        iconUrl = plugin.getIconUrl();
        url = plugin.getPluginName();
        resultUrl = plugin.getPluginResultUrlName();
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
    private void createGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {

        final ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            action.doGraph(request, response, height);
        }
    }

    /**
     * Changes the trend graph display mode.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doFlipTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {

        boolean useHealthBuilder = false;
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(getCookieName())) {
                    useHealthBuilder = Boolean.parseBoolean(cookie.getValue());
                }
            }
        }

        useHealthBuilder = !useHealthBuilder;

        final Cookie cookie = new Cookie(getCookieName(), String.valueOf(useHealthBuilder));
        final List<?> ancestors = request.getAncestors();
        final Ancestor ancestor = (Ancestor) ancestors.get(ancestors.size() - 2);
        cookie.setPath(ancestor.getUrl());
        cookie.setMaxAge(ONE_YEAR);
        response.addCookie(cookie);
        response.sendRedirect("..");
    }

    /**
     * Redirects the index page to the last result.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {

        final AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
            response.sendRedirect2(String.format("../%d/%s", build.getNumber(), resultUrl));
        }
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
    public void doTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {

        createGraph(request, response);
    }

    /**
     * Display the trend map. Delegates to the the associated {@link ResultAction}.
     * 
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doTrendMap(final StaplerRequest request, final StaplerResponse response) throws IOException {

        final ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            action.doGraphMap(request, response, height);
        }
    }
    /**
     * Return the cookie name
     * 
     * @return cookie Name
     */
    public String getCookieName() {

        return "Serenitec_plugin";
    }
    /**
     * return the Display name
     * 
     * @return display Name
     */
    public String getDisplayName() {

        return "Plugin Serenitec";
    }

    /**
     * Returns the icon URL for the side-panel in the project screen. If there is yet no valid result, then <code>null</code> is returned.
     * 
     * @return the icon URL for the side-panel in the project screen
     */
    public String getIconFileName() {

        String resultat;
        if (getLastAction() != null) {
            resultat = iconUrl;
        } else {
            resultat = null;
        }
        return resultat;
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

        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(SerenitecResultAction.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }

    /**
     * Returns the project.
     * 
     * @return the project
     */
    public final AbstractProject<?, ?> getProject() {

        return project;
    }

    /** {@inheritDoc} */
    public final String getUrlName() {

        return url;
    }

    /**
     * Returns whether we have enough valid results in order to draw a meaningful graph.
     * 
     * @return <code>true</code> if the results are valid in order to draw a graph
     */
    public final boolean hasValidResults() {

        final AbstractBuild<?, ?> build = getLastFinishedBuild();
        Boolean resultat = false;
        if (build != null) {
            final ResultAction<?> resultAction = build.getAction(SerenitecResultAction.class);
            if (resultAction != null) {
                resultat = resultAction.hasPreviousResultAction();
            }
        }
        return resultat;
    }

    /**
     * Returns whether we should display the toggle graph type links.
     * 
     * @return <code>true</code> if we should display the toggle graph type links
     */
    public final boolean isHealthinessEnabled() {

        final ResultAction<?> lastAction = getLastAction();
        boolean resultat = false;
        if (lastAction != null) {
            resultat = lastAction.getHealthReportBuilder().isEnabled();
        }
        return resultat;
    }

}
