package hudson.plugins.cppncss;

import hudson.plugins.helpers.health.HealthTarget;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 18-Mar-2008 Time: 06:11:01 To change this template use File | Settings
 * | File Templates.
 */
public class CppNCSSHealthTarget extends HealthTarget<CppNCSSHealthMetrics, CppNCSSBuildIndividualReport> {

    @DataBoundConstructor
    public CppNCSSHealthTarget(CppNCSSHealthMetrics metric, String healthy, String unhealthy, String unstable, String fail, String failNew) {
        super(metric, healthy, unhealthy, unstable, fail, failNew);
    }
}
