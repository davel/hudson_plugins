package hudson.plugins.coverage;

import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import hudson.plugins.coverage.model.Metric;
import hudson.plugins.helpers.health.HealthMetric;
import org.apache.commons.beanutils.Converter;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 29-Jun-2008 20:54:05
 */
public class CoverageHealthMetrics implements HealthMetric<CoverageBuildIndividualReport> {
// ------------------------------ FIELDS ------------------------------

    /**
     * The metric that this is based on.
     */
    private final Metric metric;
    public static final Converter CONVERTER = new Converter() {
        public Object convert(Class type, Object value) {
            return null;
        }
    };

// -------------------------- STATIC METHODS --------------------------

    /**
     * Returns all the health metrics.
     *
     * @return all the health metrics.
     */
    public static Set<CoverageHealthMetrics> values() {
        for (Metric metric : Metric.values()) {
            if (!SingletonHolder.ALL_METRICS.containsKey(metric)) {
                SingletonHolder.ALL_METRICS.put(metric, new CoverageHealthMetrics(metric));
            }
        }
        return Collections.unmodifiableSet(new HashSet<CoverageHealthMetrics>(SingletonHolder.ALL_METRICS.values()));
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private CoverageHealthMetrics(Metric metric) {
        this.metric = metric;
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface HealthMetric ---------------------

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return metric.getName();
    }

    /**
     * {@inheritDoc}
     */
    public float measure(CoverageBuildIndividualReport coverageBuildIndividualReport) {
        throw new UnsupportedOperationException("write me");
    }

    /**
     * {@inheritDoc}
     */
    public float getBest() {
        return 1.0f;
    }

    /**
     * {@inheritDoc}
     */
    public float getWorst() {
        return 0.0f;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Ensure that instances are deserialized correctly.
     *
     * @return The deserialized instance.
     *
     * @throws ObjectStreamException never.
     */
    private Object readResolve() throws ObjectStreamException {
        while (true) {
            final CoverageHealthMetrics instance = SingletonHolder.ALL_METRICS.get(metric);
            if (instance != null) {
                return instance;
            }
            SingletonHolder.ALL_METRICS.putIfAbsent(metric, this);
        }
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Holds the metrics collection singleton.
     */
    private static final class SingletonHolder {
// ------------------------------ FIELDS ------------------------------

        /**
         * The collection of metrics.
         */
        private static final ConcurrentMap<Metric, CoverageHealthMetrics> ALL_METRICS =
                new ConcurrentHashMap<Metric, CoverageHealthMetrics>();

// --------------------------- CONSTRUCTORS ---------------------------

        /**
         * Do not instantiate SingletonHolder.
         */
        private SingletonHolder() {
        }
    }
}
