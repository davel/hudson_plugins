package org.jggug.hudson.plugins.gcrawler.scm;

import java.io.FileNotFoundException;

public interface RepositoryWrapper {

    public long getLatestRevision() throws RepositoryException;

    public FileInfo findFile(String name) throws FileNotFoundException, RepositoryException;

    public FileInfo getFile(String path) throws FileNotFoundException, RepositoryException;

}
