package hudson.plugins.mibsr;

import hudson.Plugin;

/**
 * Entry point of JavaNCSS plugin.
 *
 * @author Stephen Connolly
 */
public class PluginImpl
    extends Plugin
{
    public static String DISPLAY_NAME = "Integration Test Report";

    public static String GRAPH_NAME = "Integration Test Trend";

    public static String URL = "mibsr";

    public static String ICON_FILE_NAME = "graph.gif";
}
