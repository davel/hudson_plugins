package org.jfrog.hudson;

import hudson.ProxyConfiguration;
import hudson.model.Hudson;
import hudson.util.Scrambler;
import org.apache.commons.lang.StringUtils;
import org.artifactory.build.client.ArtifactoryBuildInfoClient;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an artifactory instance.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryServer {
    private static final Logger log = Logger.getLogger(ArtifactoryServer.class.getName());

    private static final int DEFAULT_CONNECTION_TIMEOUT = 120000;    // 2 Minutes

    private final String url;
    private final String userName;
    private final String password;    // base64 scrambled password
    // Network timeout in milliseconds to use both for connection establishment and for unanswered requests
    private int timeout = DEFAULT_CONNECTION_TIMEOUT;

    /**
     * List of repository keys, last time we checked. Copy on write semantics.
     */
    private transient volatile List<String> repositories;

    @DataBoundConstructor
    public ArtifactoryServer(String url, String userName, String password, int timeout) {
        this.url = StringUtils.removeEnd(url, "/");
        this.userName = userName;
        this.password = Scrambler.scramble(password);
        this.timeout = timeout > 0 ? timeout : DEFAULT_CONNECTION_TIMEOUT;
    }

    public String getName() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return Scrambler.descramble(password);
    }

    public int getTimeout() {
        return timeout;
    }

    public List<String> getRepositoryKeys() {
        try {
            ArtifactoryBuildInfoClient client = createArtifactoryClient(userName, getPassword());
            repositories = client.getLocalRepositoriesKeys();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to obtain list of repositories: " + e.getMessage());
        }

        return repositories;
    }

    public ArtifactoryBuildInfoClient createArtifactoryClient(String userName, String password) {
        ArtifactoryBuildInfoClient client = new ArtifactoryBuildInfoClient(url, userName, password);
        client.setConnectionTimeout(timeout);

        ProxyConfiguration proxyConfiguration = Hudson.getInstance().proxy;
        if (proxyConfiguration != null) {
            client.setProxyConfiguration(proxyConfiguration.name,
                    proxyConfiguration.port,
                    proxyConfiguration.getUserName(),
                    proxyConfiguration.getPassword());
        }

        return client;
    }
}
