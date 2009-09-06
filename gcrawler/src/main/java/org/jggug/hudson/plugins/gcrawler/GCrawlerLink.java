package org.jggug.hudson.plugins.gcrawler;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.ManagementLink;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.LargeText;

@Extension
public class GCrawlerLink extends ManagementLink {

    private GCrawler crawler = GCrawler.CRAWLER;

    @Override
    public String getIconFileName() {
        return "computer.gif";
    }

    @Override
    public String getUrlName() {
        return "gcrawler";
    }

    public String getDisplayName() {
        return "GCrawler";
    }

    @Override
    public String getDescription() {
        return "Automatically search Grails project from " +
        "<a href=\"http://code.google.com/hosting/search?q=label%3Agrails\">Google Code</a> " +
        "and add build jobs.";
    }

    public List<GrailsProjectInfo> getProjectList() {
        List<GrailsProjectInfo> list = GCrawlerPlugin.getConfig().getGrailsProjectInfoList();
        if (list == null) {
            GCrawlerPlugin.getConfig().setGrailsProjectInfoList(list = new ArrayList<GrailsProjectInfo>());
        }
        return list;
    }

    public int getErrorCount(List<GrailsProjectInfo> projects) {
        if (projects == null) return 0;
        int result = 0;
        for (GrailsProjectInfo info : projects) {
            if (info.hasError()) result++;
        }
        return result;
    }

    public void doCrawl(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        if (!crawler.isActive()) {
            CrawlContext ctx = CrawlContext.newInstance();
            crawler.setCrawlerContext(ctx);
            Executors.newSingleThreadExecutor().execute(crawler);
        }
        res.sendRedirect("log");
    }

    public void doProgressLog(StaplerRequest req, StaplerResponse res) throws IOException {
        new LargeText(crawler.getCrawlerContext().getLogFile(), !crawler.isActive()).doProgressText(req, res);
    }

    public Date getLastCrawlDate() {
        return GCrawlerPlugin.getConfig().getLastCrawlDate();
    }
}
