package hudson.plugins.testabilityexplorer;

import hudson.Plugin;
import hudson.maven.MavenReporters;
import hudson.tasks.BuildStep;
import hudson.tasks.Publisher;
import hudson.plugins.testabilityexplorer.publisher.FreestylePublisher;
import hudson.plugins.testabilityexplorer.publisher.MavenPublisher;

/**
 * The Testability Explorer {@link Plugin} for Hudson.
 *
 * @author reik.schatz
 */
public class PluginImpl extends Plugin
{
    public static String DISPLAY_NAME = "Testability Explorer Report";
    public static String GRAPH_NAME = "Testability Trend";
    public static String URL = "testability";
    public static String ICON_FILE_NAME = "graph.gif";

    @Override
    public void start() throws Exception
    {
        addPublisher();
        addReporter();
    }

    protected void addPublisher()
    {
        Publisher.all().add(FreestylePublisher.DESCRIPTOR);
    }

    protected void addReporter()
    {

        MavenReporters.LIST.add(MavenPublisher.DESCRIPTOR);
    }
}
