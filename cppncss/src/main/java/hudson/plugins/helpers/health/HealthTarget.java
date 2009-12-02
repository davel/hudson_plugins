package hudson.plugins.helpers.health;

import hudson.model.HealthReport;
import hudson.model.Result;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 17-Mar-2008 Time: 12:44:28 To change this template use File | Settings
 * | File Templates.
 */
public abstract class HealthTarget<M extends HealthMetric<OBSERVABLE>, OBSERVABLE> implements Serializable {

    private final M metric;
    private final Float healthy;
    private final Float unhealthy;
    private final Float unstable;
	private final Float fail;
	private final Float failNew;

    @DataBoundConstructor
    public HealthTarget(M metric, String healthy, String unhealthy, String unstable, String fail, String failNew) {
        this.metric = metric;
		this.fail = safeParse(fail);
		this.failNew = safeParse(failNew);
        this.healthy = safeParse(healthy);
        this.unhealthy = safeParse(unhealthy);
        this.unstable = safeParse(unstable);
    }

    private static Float safeParse(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    public M getMetric() {
        return metric;
    }

    public Float getHealthy() {
        return healthy;
    }

    public Float getUnhealthy() {
        return unhealthy;
    }

    public Float getUnstable() {
        return unstable;
    }

    public Float getFail() {
		return fail;
	}

	public Float getFailNew() {
		return failNew;
	}

	public HealthReport evaluateHealth(OBSERVABLE observable, String descriptionPrefix) {
        float result = metric.measure(observable);
        float healthy = this.healthy == null ? metric.getBest() : this.healthy;
        float unhealthy = this.unhealthy == null ? metric.getWorst() : this.unhealthy;
        return new HealthReport(
                Math.max(0, Math.min(100, (int) ((result - unhealthy) / (healthy - unhealthy) * 100))),
                descriptionPrefix + metric.getName() + " (" + result + ")");
    }

    public Result evaluateStability(OBSERVABLE observable) {
        float result = metric.measure(observable);
        float resultNew = metric.measureNew(observable);
        
        float healthy = this.healthy == null ? metric.getBest() : this.healthy;
        float unhealthy = this.unhealthy == null ? metric.getWorst() : this.unhealthy;
        
        if(fail != null) {
        	if ((healthy > unhealthy && result < fail) || (healthy < unhealthy && result > fail)) {
                return Result.FAILURE;
            }
        }
        
        if(failNew != null) {
        	if ((healthy > unhealthy && resultNew < failNew) || (healthy < unhealthy && resultNew > failNew)) {
                return Result.FAILURE;
            }
        }
        
        if (unstable != null) {
            if ((healthy > unhealthy && result < unstable) || (healthy < unhealthy && result > unstable)) {
                return Result.UNSTABLE;
            }
        }
        
        return Result.SUCCESS;
    }
}
