package org.jfrog.hudson;

import hudson.Extension;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.BuildListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Records dependencies used during the build.
 *
 * @author Yossi Shaul
 */
public class MavenDependenciesRecorder extends MavenReporter {

    /**
     * All dependencies this module used, including transitive ones.
     */
    private transient Set<MavenDependency> dependencies;

    @Override
    public boolean preBuild(MavenBuildProxy build, MavenProject pom, BuildListener listener) {
        listener.getLogger().println("[HUDSON] Collecting dependencies info");
        dependencies = new HashSet<MavenDependency>();
        return true;
    }

    /**
     * Mojos perform different dependency resolution, so we add dependencies for each mojo.
     */
    @Override
    public boolean postExecute(MavenBuildProxy build, MavenProject pom, MojoInfo mojo, BuildListener listener,
            Throwable error) {
        //listener.getLogger().println("[MavenDependenciesRecorder] mojo: " + mojo.getClass() + ":" + mojo.getGoal());
        //listener.getLogger().println("[MavenDependenciesRecorder] dependencies: " + pom.getArtifacts());
        recordMavenDependencies(pom.getArtifacts());
        return true;
    }

    /**
     * Sends the collected dependencies over to the master and record them.
     */
    @Override
    public boolean postBuild(MavenBuildProxy build, MavenProject pom, BuildListener listener)
            throws InterruptedException, IOException {
        build.executeAsync(new BuildCallable<Void, IOException>() {
            // record is transient, so needs to make a copy first
            private final Set<MavenDependency> d = dependencies;

            public Void call(MavenBuild build) throws IOException, InterruptedException {
                // add the action
                build.getActions().add(new MavenDependenciesRecord(build, d));
                return null;
            }
        });
        return true;
    }

    private void recordMavenDependencies(Set<Artifact> artifacts) {
        if (artifacts != null) {
            for (Artifact dependency : artifacts) {
                MavenDependency mavenDependency = new MavenDependency();
                mavenDependency.id = dependency.getId();
                mavenDependency.groupId = dependency.getGroupId();
                mavenDependency.artifactId = dependency.getArtifactId();
                mavenDependency.version = dependency.getVersion();
                mavenDependency.classifier = dependency.getClassifier();
                mavenDependency.scope = dependency.getScope();
                mavenDependency.fileName = dependency.getFile().getName();
                dependencies.add(mavenDependency);
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends MavenReporterDescriptor {
        @Override
        public String getDisplayName() {
            return "Record Maven Dependencies";
        }

        @Override
        public MavenReporter newAutoInstance(MavenModule module) {
            return new MavenDependenciesRecorder();
        }
    }

    private static final long serialVersionUID = 1L;
}
