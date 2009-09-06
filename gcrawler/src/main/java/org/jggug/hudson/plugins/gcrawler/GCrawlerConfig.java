package org.jggug.hudson.plugins.gcrawler;

import java.util.Date;
import java.util.List;

public class GCrawlerConfig {

    private Date lastCrawlDate;

    public Date getLastCrawlDate() {
        return lastCrawlDate;
    }

    public void setLastCrawlDate(Date lastCrawlDate) {
        this.lastCrawlDate = lastCrawlDate;
    }

    private List<GrailsProjectInfo> grailsProjectInfoList;

    public List<GrailsProjectInfo> getGrailsProjectInfoList() {
        return grailsProjectInfoList;
    }

    public void setGrailsProjectInfoList(List<GrailsProjectInfo> grailsProjectInfoList) {
        this.grailsProjectInfoList = grailsProjectInfoList;
    }
}
