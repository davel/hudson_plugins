package org.jggug.hudson.plugins.gcrawler.crawlers;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryWrapper;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;

public class GrailsPluginsCrawlerTask extends GrailsProjectCrawlerTask {

    public GrailsPluginsCrawlerTask(String name, CrawlContext context, JobTemplate template, RepositoryWrapper repository) {
        super(name, context, template, repository);
    }

    @Override
    protected String getDomain() {
        return "grails.org";
    }

    @Override
    protected String getProjectUrl(GrailsProjectInfo info) {
        return String.format("http://www.grails.org/plugin/%s", info.getAppName());
    }
}
