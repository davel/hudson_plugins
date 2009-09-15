package org.jggug.hudson.plugins.gcrawler.crawlers;

import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.getFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryException;
import org.jggug.hudson.plugins.gcrawler.scm.SubversionRepository;
import org.jggug.hudson.plugins.gcrawler.scm.TrunkNotFoundException;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GrailsPluginsCrawler extends CrawlerBase {

    private static final String REPO_URL = "http://plugins.grails.org/";

    private static final Pattern LINK_PATTERN = Pattern.compile("<li><a href=\"(.*)/\">");

    private static final List<String> IGNORE_NAMES = Arrays.asList(".plugin-meta", "trunk");

    private static final JobTemplate JOB_DESCRIPTION = JobTemplate.createTemplate("grails_plugins_description.txt");

    private static final String PLUGIN_METADATA_URL = "http://plugins.grails.org/.plugin-meta/plugins-list.xml";

    public GrailsPluginsCrawler(CrawlContext context) {
        super(context);
    }

    public List<GrailsProjectInfo> crawl() throws Exception {
        List<String> pluginNames;
        pluginNames = parseHTML(getFile(REPO_URL).getText());
        GrailsCrawlerTaskService service = new GrailsCrawlerTaskService();
        // TODO add context
        mapPluginInfo(getFile(PLUGIN_METADATA_URL).getText());
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

    private static final Pattern REPO_URL_NAME_PATTERN = Pattern.compile("http://plugins\\.grails\\.org/(.*?)/.*");

    protected Map<String, GrailsPluginInfo> mapPluginInfo(String xml) throws Exception {
        Map<String, GrailsPluginInfo> result = new HashMap<String, GrailsPluginInfo>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Element element = builder.parse(in).getDocumentElement();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList releases = (NodeList) xpath.evaluate(
            "//release[../@latest-release=@version]", element, XPathConstants.NODESET);
        for (int i=0,n=releases.getLength(); i<n; i++) {
            GrailsPluginInfo info = new GrailsPluginInfo();
            Node release = releases.item(i);
            String file = xpath.evaluate("file", release);
            Matcher m = REPO_URL_NAME_PATTERN.matcher(file);
            if (m.matches()) {
                result.put(m.group(1), info);
            } else {
                System.out.println("Erorr! " + file);
            }
            info.setName(xpath.evaluate("../@name", release));
            info.setTitle(xpath.evaluate("title", release));
            info.setAuthor(xpath.evaluate("author", release));
            info.setDocumentation(xpath.evaluate("documentation", release));
            info.setDescription(xpath.evaluate("description", release));
        }
        return result;
    }
}
