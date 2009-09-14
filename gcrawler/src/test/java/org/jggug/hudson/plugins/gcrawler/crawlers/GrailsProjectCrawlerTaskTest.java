package org.jggug.hudson.plugins.gcrawler.crawlers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryException;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryWrapper;
import org.junit.Test;


public class GrailsProjectCrawlerTaskTest {

    @Test
    public void test_isUpdated_greaterThan() throws RepositoryException {
        RepositoryWrapper repository = createMock(RepositoryWrapper.class);
        expect(repository.getLatestRevision()).andReturn(3L);
        replay(repository);
        GrailsProjectCrawlerTask task = createTask(repository);

        GrailsProjectInfo info = new GrailsProjectInfo();
        info.setRevision(2);

        assertTrue(task.isUpdated(info));
        verify(repository);
    }

    @Test
    public void isUpdated_equal() throws RepositoryException {
        RepositoryWrapper repository = createMock(RepositoryWrapper.class);
        expect(repository.getLatestRevision()).andReturn(2L);
        replay(repository);
        GrailsProjectCrawlerTask task = createTask(repository);

        GrailsProjectInfo info = new GrailsProjectInfo();
        info.setRevision(2);

        assertFalse(task.isUpdated(info));
        verify(repository);
    }

    @Test
    public void isUpdated_lessThan() throws RepositoryException {
        RepositoryWrapper repository = createMock(RepositoryWrapper.class);
        expect(repository.getLatestRevision()).andReturn(1L);
        replay(repository);
        GrailsProjectCrawlerTask task = createTask(repository);

        GrailsProjectInfo info = new GrailsProjectInfo();
        info.setRevision(2);

        assertTrue(task.isUpdated(info));
        verify(repository);
    }

// TODO
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
//        task.setupProjectInfo(info, text);
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
//        task.setupProjectInfo(info, text);
//        assertEquals("GCrawler", info.getAppName());
//        assertEquals("1.1.1", info.getGrailsVersion());
//        assertEquals("2.4", info.getServletVersion());
//        assertEquals("0.1", info.getVersion());
//        assertTrue(info.getPlugins().isEmpty());
//    }

    private GrailsProjectCrawlerTask createTask(RepositoryWrapper repository) {
        return new GrailsProjectCrawlerTask(null, null, null, repository) {
            @Override
            protected String getDomain() {
                return "";
            }

            @Override
            protected String getProjectUrl(GrailsProjectInfo info) {
                return "";
            }
            
        };
    }
}
