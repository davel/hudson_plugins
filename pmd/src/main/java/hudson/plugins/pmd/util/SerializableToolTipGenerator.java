package hudson.plugins.pmd.util;

import java.io.Serializable;

import org.jfree.chart.labels.CategoryToolTipGenerator;

/**
 * A serializable {@link CategoryToolTipGenerator}.
 *
 * @author Ulli Hafner
 */
public interface SerializableToolTipGenerator extends CategoryToolTipGenerator, Serializable {
    // empty
}

