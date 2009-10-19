package hudson.plugins.jswidgets;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Extends project actions for all jobs.
 * @author mfriedenhagen
 */
@Extension
public class JsProjectActionFactory extends TransientProjectActionFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends Action> createFor(@SuppressWarnings("unchecked") AbstractProject target) {
        final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new JsJobAction(target));
        return actions;
    }

}
