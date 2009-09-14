package org.jggug.hudson.plugins.gcrawler.crawlers;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class GoogleCodeCrawlTaskTest {

    private GoogleCodeCrawlTask crawler = new GoogleCodeCrawlTask(null, null, null, null);

    @Test
    public void findLicenseLink() {
        assertEquals(
            "<a href=\"http://www.apache.org/licenses/LICENSE-2.0\" rel=\"nofollow\">Apache License 2.0</a>",
            crawler.findLicenseLink(getTestData("_AL2.txt")));
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

// TODO
//    @Test
//    public void toPluginFileName() {
//        assertEquals("HogeGrailsPlugin.groovy", crawler.toPluginFileName("hoge"));
//        assertEquals("HogeGrailsPlugin.groovy", crawler.toPluginFileName("Hoge"));
//
//        assertEquals("FooBarGrailsPlugin.groovy", crawler.toPluginFileName("FooBar"));
//        assertEquals("FooBarGrailsPlugin.groovy", crawler.toPluginFileName("fooBar"));
//
//        assertEquals("FooBarGrailsPlugin.groovy", crawler.toPluginFileName("Foo-Bar"));
//        assertEquals("FooBarGrailsPlugin.groovy", crawler.toPluginFileName("foo-Bar"));
//        assertEquals("FooBarGrailsPlugin.groovy", crawler.toPluginFileName("Foo-bar"));
//        assertEquals("FooBarGrailsPlugin.groovy", crawler.toPluginFileName("foo-bar"));
//
//        assertEquals("Foo_BarGrailsPlugin.groovy", crawler.toPluginFileName("Foo_Bar"));
//        assertEquals("Foo_BarGrailsPlugin.groovy", crawler.toPluginFileName("foo_Bar"));
//        assertEquals("Foo_barGrailsPlugin.groovy", crawler.toPluginFileName("Foo_bar"));
//        assertEquals("Foo_barGrailsPlugin.groovy", crawler.toPluginFileName("foo_bar"));
//    }

//    @Test
//    public void setupApplicationProperties() {
//        GrailsProjectInfo info = new GrailsProjectInfo();
//        String text =
//            "app.version=0.1\n" +
//            "app.grails.version=1.1.1\n" +
//            "app.name=GCrawler\n" +
//            "app.servlet.version=2.4\n" +
//            "plugins.foo=0.1\n" +
//            "plugins.bar=1.0-SNAPSHOT";
//
//        crawler.setupAppProperties(info, text);
//        assertEquals("GCrawler", info.getAppName());
//        assertEquals("1.1.1", info.getGrailsVersion());
//        assertEquals("2.4", info.getServletVersion());
//        assertEquals("0.1", info.getVersion());
//        assertEquals(Arrays.asList("foo-0.1", "bar-1.0-SNAPSHOT"), info.getPlugins());
//    }
//
//    @Test
//    public void setupApplicationProperties_NoPlugins() {
//        GrailsProjectInfo info = new GrailsProjectInfo();
//        String text =
//            "app.version=0.1\n" +
//            "app.grails.version=1.1.1\n" +
//            "app.name=GCrawler\n" +
//            "app.servlet.version=2.4";
//
//        crawler.setupAppProperties(info, text);
//        assertEquals("GCrawler", info.getAppName());
//        assertEquals("1.1.1", info.getGrailsVersion());
//        assertEquals("2.4", info.getServletVersion());
//        assertEquals("0.1", info.getVersion());
//        assertTrue(info.getPlugins().isEmpty());
//    }
}
