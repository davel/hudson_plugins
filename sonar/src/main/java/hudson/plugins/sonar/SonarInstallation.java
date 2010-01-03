/*
 * Sonar, entreprise quality control tool.
 * Copyright (C) 2007-2008 Hortis-GRC SA
 * mailto:be_agile HAT hortis DOT ch
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package hudson.plugins.sonar;

import hudson.EnvVars;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class SonarInstallation {
  private final String name;
  private final boolean disabled;
  private final String serverUrl;
  private final String databaseUrl;
  private final String databaseDriver;
  private final String databaseLogin;
  private final String databasePassword;
  private final String additionalProperties;
  private final String version;

  @DataBoundConstructor
  public SonarInstallation(String name, boolean disabled, String serverUrl, String databaseUrl,
                           String databaseDriver, String databaseLogin, String databasePassword, String additionalProperties) {
    this.name = name;
    this.disabled = disabled;
    this.serverUrl = serverUrl;
    this.databaseUrl = databaseUrl;
    this.databaseDriver = databaseDriver;
    this.databaseLogin = databaseLogin;
    this.databasePassword = databasePassword;
    this.additionalProperties = additionalProperties;
    this.version = "";
  }

  public String getName() {
    return name;
  }

  public boolean isDisabled() {
    return disabled;
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

  @Deprecated
  public String getVersion() {
    return version;
  }

  public String getPluginCallArgs(EnvVars envVars) {
    StringBuilder builder = new StringBuilder(100);
    SonarHelper.appendUnlessEmpty(builder, "sonar.jdbc.driver", envVars.expand(getDatabaseDriver()));
    SonarHelper.appendUnlessEmpty(builder, "sonar.jdbc.username", envVars.expand(getDatabaseLogin()));
    SonarHelper.appendUnlessEmpty(builder, "sonar.jdbc.password", envVars.expand(getDatabasePassword()));
    SonarHelper.appendUnlessEmpty(builder, "sonar.jdbc.url", envVars.expand(getDatabaseUrl()));
    SonarHelper.appendUnlessEmpty(builder, "sonar.host.url", envVars.expand(getServerUrl()));
    return builder.toString();
  }

  public String getServerLink() {
    return StringUtils.isEmpty(getServerUrl()) ?
        MagicNames.DEFAULT_SONAR_URL :
        getServerUrl();
  }

  private String getServerLink(String prefix, String groupId, String artifactId) {
    StringBuilder builder = new StringBuilder();
    builder.append(getServerLink())
        .append(prefix)
        .append(groupId).append(':').append(artifactId);
    return builder.toString();
  }

  /**
   * Returns URL of Sonar project dashboard.
   *
   * @param groupId    Group ID
   * @param artifactId Artifact ID
   * @return URL of Sonar project dashboard
   */
  public String getProjectLink(String groupId, String artifactId) {
    return getServerLink("/project/index/", groupId, artifactId);
  }

  public String getComponentLink(String groupId, String artifactId) {
    return getServerLink("/components/index/", groupId, artifactId);
  }
}
