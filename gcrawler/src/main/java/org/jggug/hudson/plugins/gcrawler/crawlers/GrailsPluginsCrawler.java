package org.jggug.hudson.plugins.gcrawler.crawlers;

import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.getFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryException;
import org.jggug.hudson.plugins.gcrawler.scm.SubversionRepository;
import org.jggug.hudson.plugins.gcrawler.scm.TrunkNotFoundException;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;

public class GrailsPluginsCrawler extends CrawlerBase {

    private static final String URL = "http://plugins.grails.org/";

    private static final Pattern LINK_PATTERN = Pattern.compile("<li><a href=\"(.*)/\">");

    private static final List<String> IGNORE_NAMES = Arrays.asList(".plugin-meta", "trunk");

    private static final JobTemplate JOB_DESCRIPTION = JobTemplate.createTemplate("grails_plugins_description.txt");

    public GrailsPluginsCrawler(CrawlContext context) {
        super(context);
    }

    public List<GrailsProjectInfo> crawl() throws Exception {
        List<String> pluginNames;
        pluginNames = parseHTML(getFile(URL).getText());
        GrailsCrawlerTaskService service = new GrailsCrawlerTaskService();
        for (String name : pluginNames) {
            try {
                SubversionRepository repository = new SubversionRepository(
                    String.format("http://svn.codehaus.org/grails-plugins/%s/", name));
                GrailsProjectCrawlerTask crawlerTask = new GrailsPluginsCrawlerTask(name, context, JOB_DESCRIPTION, repository);
                service.submit(crawlerTask);
            } catch (TrunkNotFoundException e) {
            } catch (RepositoryException e) {
                logger.warn(e);
            }
        }
        return service.getResults();
    }

    protected List<String> parseHTML(String html) {
        List<String> result = new ArrayList<String>();
        Matcher m = LINK_PATTERN.matcher(html);
        while (m.find()) {
            String name = m.group(1);
            if (!IGNORE_NAMES.contains(name)) {
                result.add(m.group(1));
            }
        }
        return result;
    }

}
