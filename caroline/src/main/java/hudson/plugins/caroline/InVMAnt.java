package hudson.plugins.caroline;

import hudson.Launcher;
import hudson.util.ArgumentListBuilder;
import hudson.remoting.VirtualChannel;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Ant;
import hudson.tasks.Builder;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.BuildException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.File;
import java.util.Vector;

/**
 * In Caroline grid, we can't fork new processes.
 * So run Ant in the same JVM.
 *
 * <p>
 * Obviously eventually we have to do something about this "no fork" limitation, but
 * we need some builder to get something going, and Ant was the easiest to run in the same JVM.
 *
 * @author Kohsuke Kawaguchi
 */
public class InVMAnt extends Ant {
    @DataBoundConstructor
    public InVMAnt(String targets, String antName, String antOpts, String buildFile, String properties) {
        super(targets, antName, antOpts, buildFile, properties);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        return build.getProject().getModuleRoot().act(new FileCallable<Boolean>() {
            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                Project project = new Project();
                project.init();

                project.setBaseDir(ws);

                DefaultLogger antLogger = new DefaultLogger();
                antLogger.setErrorPrintStream(listener.getLogger());
                antLogger.setOutputPrintStream(listener.getLogger());
                antLogger.setMessageOutputLevel(Project.MSG_INFO);

                project.addBuildListener(antLogger);

                ProjectHelper.configureProject(project,new File(ws,"build.xml"));

                ArgumentListBuilder args = new ArgumentListBuilder();
                args.addTokenized(getTargets());

//                project.setUserProperty("to", "World");

                try {
                    project.executeTargets(new Vector(args.toList()));
                    return true;
                } catch (BuildException e) {
                    e.printStackTrace(listener.getLogger());
                    return false;
                }
            }
        });
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Ant.DescriptorImpl {
        public DescriptorImpl() {
            super(InVMAnt.class);
            load();
        }
    }
}
