package hudson.plugins.maven_scm.bazaar;

import hudson.Extension;
import hudson.plugins.maven_scm.ProviderSpecificDescriptor;
import hudson.plugins.maven_scm.MavenSCM;
import hudson.plugins.maven_scm.PluginImpl;
import hudson.scm.SCM;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.apache.maven.scm.provider.bazaar.BazaarScmProvider;

/**
 * @author Kohsuke Kawaguchi
 */
public class BazaarDescriptor extends ProviderSpecificDescriptor {
    @Extension
    public static final BazaarDescriptor INSTANCE = new BazaarDescriptor();
    
    private BazaarDescriptor() {
        super("Bazaar","bazaar");

        PluginImpl.MANAGER.setScmProvider(provider,new BazaarScmProvider());
    }

    public String getBazaarUrl(MavenSCM scm) {
        return scm.scmUrl.substring("scm:bazaar:".length());
    }

    @Override
    public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        return new MavenSCM("scm:bazaar:"+req.getParameter("mavenscm.bazaar.url"));
    }
}
