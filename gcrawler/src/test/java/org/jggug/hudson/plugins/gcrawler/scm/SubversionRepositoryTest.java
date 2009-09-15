package org.jggug.hudson.plugins.gcrawler.scm;

import static org.junit.Assert.*;
import static java.util.regex.Pattern.compile;

import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("TODO")
public class SubversionRepositoryTest {

    @Test
    public void test_plugins() throws RepositoryException, FileNotFoundException {
        findFile("http://svn.codehaus.org/grails-plugins/grails-acegi/");
    }

    @Test(expected=TrunkNotFoundException.class)
    public void test_plugins_tags_only() throws RepositoryException, FileNotFoundException {
        findFile("http://svn.codehaus.org/grails-plugins/grails-auto-delegator/");
    }

    @Test(expected=FileNotFoundException.class)
    public void test_plugins_notFound() throws RepositoryException, FileNotFoundException {
        findFile("http://svn.codehaus.org/grails-plugins/grails-mondrian/");
    }

    @Test
    public void test_google_project_under_trunk() throws RepositoryException, FileNotFoundException {
        findFile("http://grails-examples-from-seam.googlecode.com/svn/", true);
    }

    @Test
    public void test_google_trunk() throws RepositoryException, FileNotFoundException {
        findFile("http://alumni-dsc.googlecode.com/svn/", true);
    }

    public void test_google_project_nested_dirctries() throws RepositoryException, FileNotFoundException {
        findFile("http://flexongrails.googlecode.com/svn/");
    }

    @Test(expected=FileNotFoundException.class)
    public void test_google_no_trunk() throws RepositoryException, FileNotFoundException {
        findFile("http://aloxcracetrack.googlecode.com/svn/", true);
    }

    @Test(expected=FileNotFoundException.class)
    public void test_google_nested_trunk() throws RepositoryException, FileNotFoundException {
        findFile("http://aloxcracetrack.googlecode.com/svn/", true);
    }

    @Test
    public void exist() throws RepositoryException {
        assertTrue(existsFileByPattern("http://svn.codehaus.org/grails-plugins/category/", compile("^.*Tests.groovy$")));
    }

    private void findFile(String url) throws RepositoryException, FileNotFoundException {
        findFile(url, false);
    }

    private void findFile(String url, boolean isFixedUrl) throws RepositoryException, FileNotFoundException {
        SubversionRepository repo = new SubversionRepository(url, isFixedUrl);
        FileInfo f = repo.findFile("application.properties");
        System.out.println(f.getUrl());
    }

    private boolean existsFileByPattern(String url, Pattern pattern) {
        try {
            return new SubversionRepository(url, false).existsFileByPattern(pattern);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
