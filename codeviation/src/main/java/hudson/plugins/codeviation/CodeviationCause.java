package hudson.plugins.codeviation;

import hudson.model.Cause;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class CodeviationCause extends Cause {

    @Override
    public String getShortDescription() {
        return Messages.CodeviationCause_Description();
    }
}
