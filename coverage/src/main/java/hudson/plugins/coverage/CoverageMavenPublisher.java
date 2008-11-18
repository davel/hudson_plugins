package hudson.plugins.coverage;

import java.io.File;
import java.util.Set;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.Action;
import hudson.plugins.helpers.AbstractMavenReporterImpl;
import hudson.plugins.helpers.Ghostwriter;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 29-Jun-2008 20:50:39
 */
public class CoverageMavenPublisher extends AbstractMavenReporterImpl {

    private CoverageHealthTarget[] targets;

    @DataBoundConstructor
    public CoverageMavenPublisher(CoverageHealthTarget... targets) {
        this.targets = targets == null ? new CoverageHealthTarget[0] : targets;
    }

    public CoverageHealthTarget[] getTargets() {
        return targets;
    }

    /**
     * The groupId of the Maven plugin that provides the functionality we want to report on.
     */
    private static final String PLUGIN_GROUP_ID = "org.codehaus.mojo";
    /**
     * The artifactId of the Maven plugin that provides the functionality we want to report on.
     */
    private static final String PLUGIN_ARTIFACT_ID = "javancss-maven-plugin";
    /**
     * The goal of the Maven plugin that implements the functionality we want to report on.
     */
    private static final String PLUGIN_EXECUTE_GOAL = "report";

    protected boolean isExecutingMojo(MojoInfo mojo) {
        return mojo.pluginName.matches(PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID)
                && PLUGIN_EXECUTE_GOAL.equals(mojo.getGoal());
    }

    protected Ghostwriter newGhostwriter(MavenProject pom, MojoInfo mojo) {
        String tempFileName;
        try {
            tempFileName = mojo.getConfigurationValue("tempFileName", String.class);
        } catch (ComponentConfigurationException e) {
            tempFileName = null;
        }
        if (tempFileName == null) {
            tempFileName = "javancss-raw-report.xml";
        }
        System.out.println(tempFileName);
        File baseDir = pom.getBasedir().getAbsoluteFile();
        File xmlOutputDirectory;
        try {
            xmlOutputDirectory = mojo.getConfigurationValue("xmlOutputDirector", File.class);
        } catch (ComponentConfigurationException e) {
            xmlOutputDirectory = null;
        }
        if (xmlOutputDirectory == null) {
            xmlOutputDirectory = new File(pom.getBuild().getDirectory());
        }
        System.out.println(baseDir);
        System.out.println(xmlOutputDirectory);
        String searchPath;
        String targetPath = makeDirEndWithFileSeparator(fixFilePathSeparator(xmlOutputDirectory.getAbsolutePath()));
        String baseDirPath = makeDirEndWithFileSeparator(fixFilePathSeparator(baseDir.getAbsolutePath()));
        if (targetPath.startsWith(baseDirPath)) {
            searchPath = targetPath.substring(baseDirPath.length()) + tempFileName;
        } else {
            searchPath = "**/" + tempFileName;
        }

        return new CoverageGhostwriter(searchPath, targets);
    }

    private String makeDirEndWithFileSeparator(String baseDirPath) {
        if (!baseDirPath.endsWith(File.separator)) {
            baseDirPath += File.separator;
        }
        return baseDirPath;
    }

    private String fixFilePathSeparator(String path) {
        return path.replace(File.separatorChar == '/' ? '\\' : '/', File.separatorChar);
    }

    public Action getProjectAction(MavenModule module) {
        for (MavenBuild build : module.getBuilds()) {
            if (build.getAction(CoverageBuildIndividualReport.class) != null) {
                return new CoverageProjectIndividualReport(module);
            }
        }
        return null;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * {@inheritDoc}
     */
    public MavenReporterDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public static final class DescriptorImpl extends MavenReporterDescriptor {

        /**
         * Do not instantiate DescriptorImpl.
         */
        private DescriptorImpl() {
            super(CoverageMavenPublisher.class);
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        public MavenReporter newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ConvertUtils.register(CoverageHealthMetrics.CONVERTER, CoverageHealthMetrics.class);
            return req.bindJSON(CoverageMavenPublisher.class, formData);
        }

        public Set<CoverageHealthMetrics> getMetrics() {
            return CoverageHealthMetrics.values();
        }
    }

}
