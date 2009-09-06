package org.jggug.hudson.plugins.gcrawler.crawlers;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.getFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.CrawlLogger;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;

public class GoogleCodeCrawler implements Callable<List<GrailsProjectInfo>> {

    private static final String SEARCH_URL = "http://code.google.com/hosting/search?%s";

    private static final Pattern PROJECT_NAME = compile("\"/p/([-a-zA-Z0-9]+)/\"");

    private static final Pattern NEXT_URL = compile("<a href=\".*search\\?(.*?)\">Next <b>&rsaquo;</b></a>");

    private static final List<String> IGNORE_PROJECT_NAMES = Arrays.asList("support");

    private List<GrailsProjectInfo> result = new ArrayList<GrailsProjectInfo>();

    private ExecutorService service;

    private Vector<Future<GrailsProjectInfo>> list = new Vector<Future<GrailsProjectInfo>>();

    private CrawlContext context;

    private CrawlLogger logger;

    public GoogleCodeCrawler(CrawlContext context) {
        this.context = context;
        this.logger = context.getLogger();
    }

    public List<GrailsProjectInfo> call() {
        service = Executors.newFixedThreadPool(10);
        try {
            crawl("q=label:Grails");
        } catch (FileNotFoundException e1) {
            logger.warn(e1);
        }
        for (Future<GrailsProjectInfo> future : list) {
            try {
                result.add(future.get());
            } catch (InterruptedException e) {
                logger.warn(e);
            } catch (ExecutionException e) {
                logger.warn(e);
            }
        }
        return result;
    }

    private void crawl(String query) throws FileNotFoundException {
        String url = format(SEARCH_URL, query);
        logger.info(url);
        String html = getFile(url).getText();
        Matcher nameMatcher = PROJECT_NAME.matcher(html);
        while (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (!IGNORE_PROJECT_NAMES.contains(name)) {
                list.add(service.submit(new GoogleCodeCrawlTask(name, context)));
            }
        }
        Matcher nextMatcher = NEXT_URL.matcher(html);
        if (nextMatcher.find()) {
            crawl(nextMatcher.group(1).replaceAll("&amp;", "&"));
        }
    }
}
