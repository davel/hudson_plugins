package org.jggug.hudson.plugins.gcrawler.crawlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;

public class GrailsCrawlerTaskService {

    private List<Future<GrailsProjectInfo>> futures = new ArrayList<Future<GrailsProjectInfo>>();

    private ExecutorService service = Executors.newFixedThreadPool(10);

    public void submit(GrailsProjectCrawlerTask crawlerTask) {
        futures.add(service.submit(crawlerTask));
    }

    public List<GrailsProjectInfo> getResults() throws InterruptedException, ExecutionException {
        List<GrailsProjectInfo> result = new ArrayList<GrailsProjectInfo>();
        for (Future<GrailsProjectInfo> future : futures) {
            result.add(future.get());
        }
        return result;
    }
}
