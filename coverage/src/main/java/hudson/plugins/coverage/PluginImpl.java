package hudson.plugins.coverage;

import hudson.Plugin;

/**
 * Entry point of coverage plugin.
 *
 * @author Stephen Connolly
 * @since 1.0
 * @plugin
 */
public class PluginImpl extends Plugin {
    public static String DISPLAY_NAME = "Code Coverage Report";
    public static String GRAPH_NAME = "Code Coverage Trend";
    public static String URL = "coverage";
    public static String ICON_FILE_NAME = "graph.gif";
}
