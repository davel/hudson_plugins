package hudson.plugins.coverage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.plugins.coverage.model.Measurement;
import hudson.plugins.coverage.model.Metric;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 01-Jul-2008 23:11:34
 */
public class CoverageBuildAggregatedReport extends AbstractBuildReport<MavenModuleSetBuild>
        implements MavenAggregatedReport {

    private HealthReport buildHealth = null;

    public CoverageBuildAggregatedReport(MavenModuleSetBuild build, Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        //To change body of created methods use File | Settings | File Templates.
        super(Collections.<Metric, Measurement>emptyMap());
    }

    public HealthReport getBuildHealth() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(Map<MavenModule, List<MavenBuild>> mavenModuleListMap, MavenBuild mavenBuild) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class<? extends AggregatableAction> getIndividualActionType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Action getProjectAction(MavenModuleSet mavenModuleSet) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
