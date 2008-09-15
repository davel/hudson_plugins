package hudson.plugins.coverage.model.measurements;

import hudson.plugins.coverage.model.Measurement;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 27-Jun-2008 00:19:35
 */
public class BasicCoverage implements Measurement {
    protected final int count;
    protected final int cover;

    public BasicCoverage(int cover, int count) {
        this.cover = cover;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public int getCover() {
        return cover;
    }

    public float getPercentValue() {
        if (count == 0 || cover == count) {
            return 100f;
        }
        if (cover == 0) {
            return 0;
        }
        return cover * 100f / count;
    }
}
