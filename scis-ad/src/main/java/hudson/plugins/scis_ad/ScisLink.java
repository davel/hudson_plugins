package hudson.plugins.scis_ad;

import hudson.model.ManagementLink;
import hudson.Extension;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ScisLink extends ManagementLink {
    public String getIconFileName() {
        return "/plugin/scis-ad/shield48.gif";
    }

    public String getUrlName() {
        return "http://hudson-ci.org/scis";
    }

    public String getDisplayName() {
        return "Get Support Subscription";
    }

    @Override
    public String getDescription() {
        return "Commercial support subscription available from Sun Microsystems.";
    }
}
