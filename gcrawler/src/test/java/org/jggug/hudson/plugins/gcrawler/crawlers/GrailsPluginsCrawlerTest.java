package org.jggug.hudson.plugins.gcrawler.crawlers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.CrawlLogger;
import org.junit.Test;

public class GrailsPluginsCrawlerTest {

    @Test
    public void parse() throws IOException {
        final File logFile = File.createTempFile("GrailsPluginsCrawlerTest", ".log");
        logFile.deleteOnExit();
        GrailsPluginsCrawler crawler = new GrailsPluginsCrawler(new CrawlContext() {
            @Override
            public CrawlLogger getLogger() {
                return new CrawlLogger(logFile);
            }
        });
        List<String> actual = crawler.parseHTML(getTestData("_index.html"));
        List<String> expected = Arrays.asList(
            "category",
            "grails-acegi",
            "grails-activemq"
        );
        assertEquals(expected.toString(), actual.toString());
    }

    private String getTestData(String suffix) {
        String name = getClass().getName().replaceAll("\\.", "/") + suffix;
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        try {
            return IOUtils.toString(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
