package org.jggug.hudson.plugins.gcrawler.scm;

import org.apache.commons.lang.exception.NestableException;

public class RepositoryException extends NestableException {

    private static final long serialVersionUID = 1L;

    public RepositoryException() {
        super();
    }

    public RepositoryException(Exception cause) {
        super(cause);
    }
}
