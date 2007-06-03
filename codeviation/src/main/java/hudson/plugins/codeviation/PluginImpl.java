/* Copyright (c) 2007, http://www.codeviation.org project 
 * This program is made available under the terms of the MIT License. 
 */

package hudson.plugins.codeviation;

import hudson.Plugin;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Publisher;
import org.codeviation.model.vcs.CVSMetric;
import org.openide.util.Lookup;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @plugin
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        System.setProperty("org.openide.util.Lookup",HPILookup.class.getName());
        if (! (Lookup.getDefault() instanceof HPILookup)) {
            HPILookup.logger.severe("Lookup is not HPILookup.");
        }
        // plugins normally extend Hudson by providing custom implementations
        // of 'extension points'. In this example, we'll add one builder.
        BuildWrappers.WRAPPERS.add(PAntWrapper.DESCRIPTOR);
        Publisher.PUBLISHERS.add(CodeviationPublisher.DESCRIPTOR);
        CVSMetric.setUpdateCVS(false);
    }
}
