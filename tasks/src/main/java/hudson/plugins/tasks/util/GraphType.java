package hudson.plugins.tasks.util;

import org.jfree.chart.JFreeChart;

/**
 * The available types for the trend graph.
 */
public enum GraphType {
    /** No graph at all. */
    NONE {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction) {
            return PRIORITY.createGraph(null, healthDescriptor, resultAction); // should never get invoked
        }

        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String url) {
            return PRIORITY.createGraph(null, healthDescriptor, resultAction, url); // should never get invoked
        }
    },
    /** Warnings by priority. */
    PRIORITY {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction) {
            return new PriorityGraph().create(configuration, resultAction);
        }

        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String url) {
            return new PriorityGraph().create(configuration, resultAction, url);
        }
    },
    /** Warnings by new versus fixed. */
    NEW_VS_FIXED {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction) {
            return new NewVersusFixedGraph().create(configuration, resultAction);
        }

        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String url) {
            return new NewVersusFixedGraph().create(configuration, resultAction, url);
        }
    },
    /** Warnings by health trend. */
    HEALTH {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction) {
            return new HealthGraph(healthDescriptor).create(configuration, resultAction);
        }

        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String url) {
            return new HealthGraph(healthDescriptor).create(configuration, resultAction, url);
        }
    };


    /**
     * Creates the graph.
     *
     * @param configuration
     *            the configuration parameters
     * @param healthDescriptor
     *            the health descriptor
     * @param resultAction
     *            the action to start the graph with
     * @return the graph
     */
    public abstract JFreeChart createGraph(GraphConfiguration configuration, AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction);

    /**
     * Creates the graph.
     *
     * @param configuration
     *            the configuration parameters
     * @param healthDescriptor
     *            the health descriptor
     * @param resultAction
     *            the action to start the graph with
     * @param url
     *            base URL of the graph links
     * @return the graph
     */
    public abstract JFreeChart createGraph(GraphConfiguration configuration, AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String url);
}