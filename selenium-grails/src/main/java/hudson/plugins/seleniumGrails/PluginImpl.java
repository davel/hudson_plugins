package hudson.plugins.seleniumGrails;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
			BuildStep.PUBLISHERS.addRecorder(SeleniumGrailsPublisher.DESCRIPTOR);
      BuildStep.BUILDERS.add(SeleniumGrailsBuilder.DESCRIPTOR);
    }
}
