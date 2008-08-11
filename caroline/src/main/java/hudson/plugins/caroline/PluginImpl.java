package hudson.plugins.caroline;

import hudson.Plugin;
import hudson.tasks.BuildStep;
import hudson.tasks.Ant;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.BUILDERS.remove(Ant.DESCRIPTOR);
        BuildStep.BUILDERS.add(InVMAnt.DESCRIPTOR);
    }
}
