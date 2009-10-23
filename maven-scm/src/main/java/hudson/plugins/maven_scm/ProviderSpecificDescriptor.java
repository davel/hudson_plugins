package hudson.plugins.maven_scm;

import hudson.scm.SCMDescriptor;

/**
 * {@link SCMDescriptor} for {@link MavenSCM} in case providers are statically known to us.
 *
 * @author Kohsuke Kawaguchi
 * @see GenericMavenSCMDescriptor
 */
public abstract class ProviderSpecificDescriptor extends AbstractMavenSCMDescriptor {
    private final String displayName;
    public final String provider;

    public ProviderSpecificDescriptor(String displayName, String provider) {
        this.displayName = displayName;
        this.provider = provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getHelpFile() {
        return "/plugin/maven-scm/"+ provider +"/help.html";
    }

    @Override
    public String getConfigPage() {
        return getViewPage(clazz, "config-"+provider+".jelly");
    }
}
