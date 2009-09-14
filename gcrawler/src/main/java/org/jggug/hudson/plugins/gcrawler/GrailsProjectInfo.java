package org.jggug.hudson.plugins.gcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class GrailsProjectInfo {

    private String name;

    private String scmUrl;

    private String projectUrl;

    private long parseTime;

    private String type;

    private String domain;

    private String errorMessage;

    private String appName;

    private String version;

    private String servletVersion;

    private String grailsVersion;

    private String licenseLink;

    private long revision;

    private List<String> plugins = new ArrayList<String>();

    public String getVersion() {
        return version;
    }

    public String getServletVersion() {
        return servletVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getGrailsVersion() {
        return grailsVersion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setServletVersion(String servletVersion) {
        this.servletVersion = servletVersion;
    }

    public void setGrailsVersion(String grailsVersion) {
        this.grailsVersion = grailsVersion;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public long getParseTime() {
        return parseTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public void setParseTime(long parseTime) {
        this.parseTime = parseTime;
    }

    public String toJobName() {
        if (StringUtils.isEmpty(domain)) {
            return name;
        }
        return String.format("%s.%s", name, domain);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", name)
            .append("grailsVersion", grailsVersion)
            .append("servletVersion", servletVersion)
            .append("version", version)
            .append("scmUrl", scmUrl)
            .append("projectUrl", projectUrl)
            .append("parseTime", parseTime)
            .append("plugins", plugins)
            .toString();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean hasError() {
        return errorMessage != null;
    }

    public String getLicenseLink() {
        return licenseLink;
    }

    public void setLicenseLink(String licenseLink) {
        this.licenseLink = licenseLink;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public void addPlugin(String plugin) {
        plugins.add(plugin);
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }
}
