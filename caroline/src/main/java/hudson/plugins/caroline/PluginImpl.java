package hudson.plugins.caroline;

import hudson.Plugin;
import hudson.tasks.Ant;
import hudson.tasks.Builder;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    @Override
    public void start() throws Exception {
        Builder.all().remove(Builder.all().get(Ant.DescriptorImpl.class));
    }
}
