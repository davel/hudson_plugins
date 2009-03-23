package hudson.plugins.testabilityexplorer.publisher;

import org.apache.maven.project.MavenProject;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenReporter;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.testabilityexplorer.helpers.BuildProxy;
import hudson.plugins.testabilityexplorer.helpers.ParseDelegate;
import hudson.plugins.testabilityexplorer.utils.TypeConverterUtil;

import java.io.IOException;

/**
 * A {@link MavenReporter} that will do the parsing of the report file(s) in it's
 * {@link MavenReporter#postBuild} method.
 *
 * @author reik.schatz
 */
public abstract class AbstractMavenReporterImpl extends MavenReporter implements
        ExtractAndBuildDelegate {

    protected static final double DEFAULT_WEIGHT = 1.5;

    @Override
    public boolean postBuild(final MavenBuildProxy build, final MavenProject pom,
            final BuildListener listener) throws InterruptedException, IOException {
        try {
            build.execute(getBuildCallable(listener));
        } catch (Throwable e) {
            build.setResult(Result.FAILURE);
            return false;
        }

        return true;
    }

    /**
     * Returns a BuildCallable that will delegate parsing of the reports.
     *
     * @return
     */
    MavenBuildProxy.BuildCallable<Void, IOException> getBuildCallable(final BuildListener listener) {
        return new MavenBuildProxy.BuildCallable<Void, IOException>() {

            public Void call(final MavenBuild mavenBuild) throws IOException {
                if (mavenBuild.isBuilding()) {
                    AbstractProject project = mavenBuild.getProject();
                    BuildProxy buildProxy = new BuildProxy(getModuleRoot(project),
                            newStatisticsParser(), newDetailBuilder(), newReportBuilder());
                    ParseDelegate parseDelegate = newParseDelegate();
                    parseDelegate.perform(buildProxy, listener);
                    buildProxy.updateBuild(mavenBuild);
                }
                return null;
            }
        };
    }

    /**
     * Returns the {@link FilePath} to the module root of the given project.
     *
     * @param project AbstractProject
     * @return FilePath
     */
    FilePath getModuleRoot(final AbstractProject project) {
        return project.getModuleRoot();
    }

    protected boolean toBool(String value, boolean defaultValue) {
        if (null != value) {
            return Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }

    protected int toInt(String value, int defaultValue) {
        return TypeConverterUtil.toInt(value, defaultValue);
    }

    protected double toDouble(String value, double defaultValue) {
        return TypeConverterUtil.toDouble(value, defaultValue);
    }
}
