package hudson.plugins.hello_world;

import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
 * will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class HelloWorldBuilder extends Builder {

    private final String name;

    HelloWorldBuilder(String name) {
        this.name = name;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    public boolean perform(Build build, Launcher launcher, BuildListener listener) {
        // this is where you 'build' the project
        // since this is a dummy, we just say 'hello world' and call that a build

        // this also shows how you can consult the global configuration of the builder
        if(DESCRIPTOR.useFrench())
            listener.getLogger().println("Bonjour, "+name+"!");
        else
            listener.getLogger().println("Hello, "+name+"!");
        return true;
    }

    public Descriptor<Builder> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    public static final class DescriptorImpl extends Descriptor<Builder> {
        DescriptorImpl() {
            super(HelloWorldBuilder.class);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Say hello world";
        }

        public boolean configure(HttpServletRequest req) throws FormException {
            // to persist global configuration information,
            // set that to properties and call save().
            getProperties().put("useFrench",req.getParameter("hello_world.useFrench"));
            save();
            return super.configure(req);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         */
        public boolean useFrench() {
            return getProperties().get("useFrench")!=null;
        }

        /**
         * Creates a new instance of {@link HelloWorldBuilder} from a submitted form.
         */
        public HelloWorldBuilder newInstance(StaplerRequest req) throws FormException {
            // see config.jelly and you'll find "hello_world.name" form entry.
            return new HelloWorldBuilder(req.getParameter("hello_world.name"));
        }
    }
}
