package hudson.plugins.maven_scm;

import hudson.Extension;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCM;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link SCMDescriptor} for {@link MavenSCM} in case providers are not statically known to us.
 *
 * @author Kohsuke Kawaguchi
 */
public class GenericMavenSCMDescriptor extends AbstractMavenSCMDescriptor {
    @Extension
    public static final GenericMavenSCMDescriptor INSTANCE = new GenericMavenSCMDescriptor();

    private GenericMavenSCMDescriptor() {
    }

    public String getDisplayName() {
        return "Other Maven SCM";
    }

    @Override
    public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        return new MavenSCM(req.getParameter("mavenscm.scmUrl"));
    }
}
