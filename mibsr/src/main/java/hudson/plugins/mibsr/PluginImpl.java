package hudson.plugins.mibsr;

import hudson.Plugin;
import hudson.maven.MavenReporters;
import hudson.tasks.BuildStep;

/**
 * Entry point of JavaNCSS plugin.
 *
 * @author Stephen Connolly
 */
public class PluginImpl
    extends Plugin
{
    /**
     * {@inheritDoc}
     */
    public void start()
        throws Exception
    {
        BuildStep.PUBLISHERS.add( MIBSRPublisher.DESCRIPTOR );
        MavenReporters.LIST.add( MIBSRMavenPublisher.DESCRIPTOR );
    }

    public static String DISPLAY_NAME = "Integration Test Report";

    public static String GRAPH_NAME = "Integration Test Trend";

    public static String URL = "mibsr";

    public static String ICON_FILE_NAME = "graph.gif";
}
