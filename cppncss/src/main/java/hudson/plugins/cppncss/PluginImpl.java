package hudson.plugins.cppncss;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point of CppNCSS plugin.
 *
 * @author Stephen Connolly
 * @author Shaohua Wen
 * @plugin
 */
public class PluginImpl extends Plugin {
    /**
     * {@inheritDoc}
     */
    public void start() throws Exception {
        BuildStep.PUBLISHERS.add(CppNCSSPublisher.DESCRIPTOR);
    }

    public static String DISPLAY_NAME = "Cpp NCSS Report";
    public static String GRAPH_NAME = "Cpp NCSS Trend";
    public static String URL = "cppncss";
    public static String ICON_FILE_NAME = "graph.gif";
}
