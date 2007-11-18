package hudson.plugins.cobertura;

import hudson.FilePath;
import hudson.Util;
import hudson.maven.*;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.cobertura.renderers.SourceCodePainter;
import hudson.plugins.cobertura.targets.CoverageResult;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stephen
 * Date: 17-Nov-2007
 * Time: 19:08:46
 * To change this template use File | Settings | File Templates.
 */
public class MavenCoberturaPublisher extends MavenReporter {

    /**
     * {@inheritDoc}
     */
    public boolean postExecute(MavenBuildProxy build, MavenProject pom, MojoInfo mojo, BuildListener listener,
                               Throwable error) throws InterruptedException, IOException {
        if (!mojo.pluginName.matches("org.codehaus.mojo", "cobertura-maven-plugin"))
            return true;

        if (!mojo.getGoal().equals("cobertura"))
            return true;

        boolean haveXMLReport = false;
        File outputDir;
        try {
            outputDir = mojo.getConfigurationValue("outputDirectory", File.class);
            if (!outputDir.exists()) {
                // cobertura-maven-plugin will not generate a report for non-java projects
                return true;
            }

            String[] formats = mojo.getConfigurationValue("formats", String[].class);
            for (String o : formats) {
                if ("xml".equals(o.trim())) {
                    haveXMLReport = true;
                    break;
                }
            }
            if (!haveXMLReport) {
                listener.getLogger().println("[HUDSON] If the cobertura plugin needs was configured to generate xml reports, e.g.");
                listener.getLogger().println("[HUDSON]     ...");
                listener.getLogger().println("[HUDSON]     <plugin>");
                listener.getLogger().println("[HUDSON]       <groupId>org.codehaus.mojo</groupId>");
                listener.getLogger().println("[HUDSON]       <artifactId>codehaus-maven-plugin</artifactId>");
                listener.getLogger().println("[HUDSON]       ...");
                listener.getLogger().println("[HUDSON]       <configuration>");
                listener.getLogger().println("[HUDSON]         ...");
                listener.getLogger().println("[HUDSON]         <formats>");
                listener.getLogger().println("[HUDSON]           ...");
                listener.getLogger().println("[HUDSON]           <format>xml</format> <!-- ensure this format is present -->");
                listener.getLogger().println("[HUDSON]           ...");
                listener.getLogger().println("[HUDSON]         </formats>");
                listener.getLogger().println("[HUDSON]         ...");
                listener.getLogger().println("[HUDSON]       </configuration>");
                listener.getLogger().println("[HUDSON]       ...");
                listener.getLogger().println("[HUDSON]     </plugin>");
                listener.getLogger().println("[HUDSON]     ...");
                listener.getLogger().println("[HUDSON] Code coverage reports would be enabled");
                build.setResult(Result.UNSTABLE);
                return true;
            }
        } catch (ComponentConfigurationException e) {
            e.printStackTrace(listener.fatalError("Unable to obtain configuration from cobertura mojo"));
            build.setResult(Result.UNSTABLE);
            return true;
        }

        File reportFile = new File(outputDir, "coverage.xml");
        if (!reportFile.exists()) {
            listener.getLogger().println("[HUDSON] Cobertura report not generated (probably this module is not a java module)");
            return true;
        }

        FilePath reportFilePath = new FilePath(reportFile);

        FilePath target = build.getRootDir();

        try {
            target.mkdirs();
            listener.getLogger().println("[HUDSON] Recording coverage results");
            reportFilePath.copyTo(target.child("coverage.xml"));
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to copy " + reportFilePath + " to " + target));
            build.setResult(Result.FAILURE);
        }

        CoverageResult result = null;
        Set<String> sourcePaths = new HashSet<String>();

        try {
            result = CoberturaCoverageParser.parse(reportFile, result, sourcePaths);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to parse " + reportFilePath));
            build.setResult(Result.FAILURE);
        }

        if (result != null) {
            final FilePath paintedSourcesPath = build.getProjectRootDir().child("cobertura");
            paintedSourcesPath.mkdirs();
            SourceCodePainter painter = new SourceCodePainter(paintedSourcesPath, sourcePaths,
                    result.getPaintedSources());

            new FilePath(pom.getBasedir()).act(painter);

            // TODO the build action 
        } else {
            listener.getLogger().println("[HUDSON] Unable to parse coverage results.");
            build.setResult(Result.FAILURE);
            return true;
        }

        // TODO the project level action
        // build.registerAsProjectAction(this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Action getProjectAction(MavenModule project) {
        return new CoberturaProjectAction(project);
    }

    /**
     * {@inheritDoc}
     */
    public MavenReporterDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends MavenReporterDescriptor {
        /**
         * Do not instantiate DescriptorImpl.
         */
        private DescriptorImpl() {
            super(MavenCoberturaPublisher.class);
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Publish Cobertura Coverage Report";
        }

        /**
         * {@inheritDoc}
         */
        public MavenReporter newAutoInstance(MavenModule mavenModule) {
            return new MavenCoberturaPublisher();
        }
    }

    private static final long serialVersionUID = 1L;
}
