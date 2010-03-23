package hudson.plugins.tasks.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsTablePortlet;
import hudson.plugins.tasks.Messages;
import hudson.plugins.tasks.TasksProjectAction;
import hudson.plugins.view.dashboard.DashboardPortlet;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A dashboard that shows a table with the number of warnings in a job.
 *
 * @author Ulli Hafner
 */
public class WarningsTablePortlet extends AbstractWarningsTablePortlet {
    /**
     * Creates a new instance of {@link WarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    @DataBoundConstructor
    public WarningsTablePortlet(final String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return TasksProjectAction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return "tasks";
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    public static class WarningsPerJobDescriptor extends Descriptor<DashboardPortlet> {
        /**
         * Creates a new descriptor if the dashboard-view plug-in is installed.
         *
         * @return the descriptor or <code>null</code> if the dashboard view is not installed
         */
        @Extension
        public static WarningsPerJobDescriptor newInstance() {
            if (Hudson.getInstance().getPlugin("dashboard-view") != null) {
                return new WarningsPerJobDescriptor();
            }
            return null;
        }

        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsTable();
        }
    }
}

