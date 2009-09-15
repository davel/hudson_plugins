package org.jggug.hudson.plugins.gcrawler.crawlers;

import static java.util.regex.Pattern.compile;
import hudson.model.FreeStyleProject;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.plugins.googlecode.GoogleCodeRepositoryBrowser;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryWrapper;
import org.jggug.hudson.plugins.gcrawler.util.HttpUtils;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;

class GoogleCodeCrawlTask extends GrailsProjectCrawlerTask {

    private static final Pattern LICENSE = compile("<a .*? rel=\"nofollow\">.*?</a>");

    private static final String LISENCE_NA = "N/A";

    public GoogleCodeCrawlTask(String name, CrawlContext context, JobTemplate template, RepositoryWrapper repository) {
        super(name, context, template, repository);
    }

    @Override
    protected void setupJob(FreeStyleProject job, GrailsProjectInfo info) throws Exception {
        super.setupJob(job, info);
        job.addProperty(new GoogleCodeProjectProperty(info.getProjectUrl()));
        job.setScm(new SubversionSCM(Arrays.asList(new ModuleLocation(info.getScmUrl(), info.getName())), true,
                new GoogleCodeRepositoryBrowser(), ""));
    }

    @Override
    protected void setupProjectInfo(GrailsProjectInfo info, String appProperties) {
        super.setupProjectInfo(info, appProperties);
        try {
            info.setLicenseLink(findLicenseLink(HttpUtils.getFile(info.getProjectUrl()).getText()));
        } catch (FileNotFoundException e) {
            info.setLicenseLink(LISENCE_NA);
        }
    }

    @Override
    protected String getProjectUrl(GrailsProjectInfo info) {
        return String.format("http://code.google.com/p/%s/", info.getName());
    }

    @Override
    protected String getDomain() {
        return "googlecode.com";
    }

    protected String findLicenseLink(String text) {
        Matcher m = LICENSE.matcher(text);
        return m.find() ? m.group() : LISENCE_NA;
    }
}