package org.jggug.hudson.plugins.gcrawler.crawlers;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.getFile;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryException;
import org.jggug.hudson.plugins.gcrawler.scm.SubversionRepository;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;

public class GoogleCodeCrawler extends CrawlerBase {

    private static final String SEARCH_URL = "http://code.google.com/hosting/search?%s";

    private static final Pattern PROJECT_NAME = compile("\"/p/([-a-zA-Z0-9]+)/\"");

    private static final Pattern NEXT_URL = compile("<a href=\".*search\\?(.*?)\">Next <b>&rsaquo;</b></a>");

    private static final List<String> IGNORE_PROJECT_NAMES = Arrays.asList("support");

    private static final JobTemplate JOB_DESCRIPTION = JobTemplate.createTemplate("google_grails_description.txt");

    private GrailsCrawlerTaskService service;

    public GoogleCodeCrawler(CrawlContext context) {
        super(context);
    }

    public List<GrailsProjectInfo> crawl() throws Exception {
        service = new GrailsCrawlerTaskService();
        try {
            crawl("q=label:Grails");
        } catch (FileNotFoundException e1) {
            logger.warn(e1);
        }
        return service.getResults();
    }

    private void crawl(String query) throws FileNotFoundException {
        String url = format(SEARCH_URL, query);
        logger.info(url);
        String html = getFile(url).getText();
        Matcher nameMatcher = PROJECT_NAME.matcher(html);
        while (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (!IGNORE_PROJECT_NAMES.contains(name)) {
                try {
                    SubversionRepository repository =
                        new SubversionRepository(format("http://%s.googlecode.com/svn/", name), true);
                    GoogleCodeCrawlTask crawlTask = new GoogleCodeCrawlTask(name, context, JOB_DESCRIPTION, repository);
                    service.submit(crawlTask);
                } catch (RepositoryException e) {
                    logger.warn(e);
                }
            }
        }
        Matcher nextMatcher = NEXT_URL.matcher(html);
        if (nextMatcher.find()) {
            crawl(nextMatcher.group(1).replaceAll("&amp;", "&"));
        }
    }
}
