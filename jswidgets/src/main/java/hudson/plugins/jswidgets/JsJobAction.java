package hudson.plugins.jswidgets;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class implements the JS widgets pages for a job.
 * 
 * @author mfriedenhagen
 */
public class JsJobAction extends JsBaseAction {
    
    /** The Logger. */
    private static final Logger LOG = Logger.getLogger(JsJobAction.class.getName());

    /** The project. */
    private final transient AbstractProject<?, ?> project;

    /**
     * @param project
     *            the job for which the health report will be generated.
     */
    public JsJobAction(AbstractProject<?, ?> project) {
        this.project = project;
        // add the JsBuildAction to all run builds, JsRunListener will append this to the others.
        final List<?> builds = (List<?>) project.getBuilds();
        for (Object object : builds) {            
            final AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) object;
            build.addAction(new JsBuildAction(build));
            LOG.fine(build.toString() + ":" + build.getActions().toString());
        }

    }

    /**
     * Returns the job for which the health report will be generated.
     * 
     * @return job
     */
    public AbstractProject<?, ?> getProject() {
        return project;
    }

    /**
     * Returns the description of the job without line feeds as this will break the Javascript output.
     * 
     * @return the description in one line.
     */
    public String getJobDescription() {
        return project.getDescription().replace("\n", "").replace("\r", "");
    }

}
