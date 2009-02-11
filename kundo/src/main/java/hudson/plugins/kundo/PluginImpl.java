package hudson.plugins.kundo;

import hudson.Plugin;
import hudson.tasks.BuildStep;

public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.BUILDERS.add( Kundo.DESCRIPTOR );
    }
}
