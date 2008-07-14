package hudson.plugins.sonar;

import org.kohsuke.stapler.DataBoundConstructor;

public final class SonarInstallation {
  private final String name;
  private final boolean disabled;
  private final String version;
  private final String serverUrl;
  private final String databaseUrl;
  private final String databaseDriver;
  private final String databaseLogin;
  private final String databasePassword;
  private final String additionalProperties;


  @DataBoundConstructor
  public SonarInstallation(String name, boolean disabled, String version, String serverUrl, String databaseUrl, String databaseDriver, String databaseLogin, String databasePassword, String additionalProperties) {
    this.name = name;
    this.disabled = disabled;
    this.version = version;
    this.serverUrl = serverUrl;
    this.databaseUrl = databaseUrl;
    this.databaseDriver = databaseDriver;
    this.databaseLogin = databaseLogin;
    this.databasePassword = databasePassword;
    this.additionalProperties = additionalProperties;
  }

  public String getName() {
    return name;
  }


  public boolean isDisabled() {
    return disabled;
  }

  public String getVersion() {
    return version;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getDatabaseUrl() {
    return databaseUrl;
  }

  public String getDatabaseDriver() {
    return databaseDriver;
  }

  public String getDatabaseLogin() {
    return databaseLogin;
  }

  public String getDatabasePassword() {
    return databasePassword;
  }

  public String getAdditionalProperties() {
    return additionalProperties;
  }
}

