package hudson.plugins.javancss;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.HealthReport;
import hudson.plugins.helpers.BuildProxy;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.javancss.parser.Statistic;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 23:16:52
 */
public class JavaNCSSGhostwriter
        implements Ghostwriter,
        Ghostwriter.MasterGhostwriter,
        Ghostwriter.SlaveGhostwriter {

    private final String reportFilenamePattern;

    private final JavaNCSSHealthTarget[] targets;

    public JavaNCSSGhostwriter(String reportFilenamePattern, JavaNCSSHealthTarget... targets) {
        this.reportFilenamePattern = reportFilenamePattern;
        this.targets = targets;
    }

    public boolean performFromMaster(AbstractBuild<?, ?> build, FilePath executionRoot, BuildListener listener)
            throws InterruptedException, IOException {
        return true;
    }

    public boolean performFromSlave(BuildProxy build, BuildListener listener) throws InterruptedException, IOException {
        FilePath[] paths = build.getExecutionRootDir().list(reportFilenamePattern);
        Collection<Statistic> results = null;
        Set<String> parsedFiles = new HashSet<String>();
        for (FilePath path : paths) {
            final String pathStr = path.getRemote();
            if (!parsedFiles.contains(pathStr)) {
                parsedFiles.add(pathStr);
                try {
                    Collection<Statistic> result = Statistic.parse(new File(pathStr));
                    if (results == null) {
                        results = result;
                    } else {
                        results = Statistic.merge(results, result);
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace(listener.getLogger());
                }
            }
        }
        if (results != null) {
            JavaNCSSBuildIndividualReport action = new JavaNCSSBuildIndividualReport(results);
            if (targets != null && targets.length > 0) {
                HealthReport r = null;
                for (JavaNCSSHealthTarget target : targets) {
                    r = HealthReport.min(r, target.evaluateHealth(action));
                }
                action.setBuildHealth(r);
            }
            build.getActions().add(action);
        }
        return true;
    }
}
