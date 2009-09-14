package org.jggug.hudson.plugins.gcrawler.crawlers;

import java.util.List;
import java.util.concurrent.Callable;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.CrawlLogger;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;

public abstract class CrawlerBase implements Callable<List<GrailsProjectInfo>> {

    protected CrawlContext context;

    protected CrawlLogger logger;

    public CrawlerBase(CrawlContext context) {
        this.context = context;
        this.logger = context.getLogger();
    }

    public List<GrailsProjectInfo> call() throws Exception {
        return crawl();
    }

    protected abstract List<GrailsProjectInfo> crawl() throws Exception;
}
