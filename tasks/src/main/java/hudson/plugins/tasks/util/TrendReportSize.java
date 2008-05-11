package hudson.plugins.tasks.util;

import org.apache.commons.lang.StringUtils;

/**
 * Controls the size of a trend report.
 *
 * @author Ulli Hafner
 */
public class TrendReportSize {
    /** Default height of the graph. */
    private static final int HEIGHT = 200;
    /** Actual height of the trend graph. */
    private final int trendHeight;

    /**
     * Creates a new instance of <code>TrendReportSize</code>.
     * @param height
     *            the height of the trend graph
     */
    public TrendReportSize(final String height) {
        int actualHeight = HEIGHT;
        if (!StringUtils.isEmpty(height)) {
            try {
                actualHeight = Math.max(50, Integer.valueOf(height));
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        trendHeight = actualHeight;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public int getHeight() {
        return trendHeight;
    }
}

