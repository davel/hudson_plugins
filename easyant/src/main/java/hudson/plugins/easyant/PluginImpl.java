package hudson.plugins.easyant;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point of EasyAnt plugin
 * @author Jean Louis Boudart
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.BUILDERS.add(EasyAnt.DESCRIPTOR);
    }
}
