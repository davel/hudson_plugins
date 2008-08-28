package hudson.plugins.deploy;

import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.net.URL;

/**
 * JBoss 3.x.
 * 
 * @author Kohsuke Kawaguchi
 */
public class JBoss3xAdapter extends JBossAdapter {
    @DataBoundConstructor
    public JBoss3xAdapter(String url, String password, String userName) {
        super(url, password, userName);
    }

    public String getContainerId() {
        return "jboss3x";
    }

    public Descriptor<ContainerAdapter> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final Descriptor<ContainerAdapter> DESCRIPTOR = new Descriptor<ContainerAdapter>(JBoss3xAdapter.class) {
        public String getDisplayName() {
            return "JBoss 3.x";
        }
    };
}
