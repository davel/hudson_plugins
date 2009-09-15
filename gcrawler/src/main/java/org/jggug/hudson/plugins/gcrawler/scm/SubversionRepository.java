package org.jggug.hudson.plugins.gcrawler.scm;

import static org.tmatesoft.svn.core.SVNNodeKind.DIR;
import static org.tmatesoft.svn.core.SVNNodeKind.FILE;
import static org.tmatesoft.svn.core.SVNURL.parseURIDecoded;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

public class SubversionRepository implements RepositoryWrapper {

    static {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    private SVNRepository repository;

    public SubversionRepository(String repositoryURL) throws RepositoryException {
        this(repositoryURL, false);
    }

    public SubversionRepository(String repositoryURL, boolean isFixedUrl) throws RepositoryException {
        try {
            SVNURL svnUrl = SVNURL.parseURIDecoded(repositoryURL);
            repository = SVNClientManager.newInstance().createRepository(svnUrl, false);
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
        if (isFixedUrl) return;
        try {
            for (SVNDirEntry entry : dir("")) {
                if (entry.getKind() == DIR && entry.getName().equals("trunk")) {
                    repository = SVNClientManager.newInstance().createRepository(parseURIDecoded(repositoryURL + "trunk"), false);
                    return;
                }
            }
            throw new TrunkNotFoundException();
        } catch (SVNException e) {
            try {
                repository = SVNClientManager.newInstance().createRepository(parseURIDecoded(repositoryURL + "/trunk"), false);
            } catch (SVNException e1) {
                throw new RepositoryException(e1);
            }
        }
    }

    public FileInfo findFile(String name) throws FileNotFoundException, RepositoryException {
        boolean hasTrunk = repository.getLocation().toString().endsWith("trunk");
        return findFile((hasTrunk ? "" : "trunk"), name);
    }

    private List<SVNDirEntry> dir(String path) throws SVNException {
        List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
        repository.getDir(path, -1, false, entries);
        return entries;
    }

    private FileInfo findFile(String parent, String name) throws FileNotFoundException, RepositoryException {
        List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
        try {
            repository.getDir(parent, -1, false, entries);
            Collections.sort(entries, FileFirstSVNDirEntryComparator.C);
            for (SVNDirEntry entry : entries) {
                if (entry.getKind() == FILE) {
                    if (entry.getName().equals(name)) {
                        return getFile((StringUtils.isEmpty(parent) ? "" : parent.concat("/")).concat(entry.getRelativePath()));
                    }
                }
                else if (entry.getKind() == DIR) {
                    try {
                        return findFile((StringUtils.isEmpty(parent) ? "" : parent + "/") + entry.getName(), name);
                    } catch (FileNotFoundException e) {}
                }
            }
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
        throw new FileNotFoundException(name);
    }

    public boolean existsFileByPattern(Pattern pattern) throws RepositoryException {
        boolean hasTrunk = repository.getLocation().toString().endsWith("trunk");
        return existsFileByPattern((hasTrunk ? "" : "trunk"), pattern);
    }

    private boolean existsFileByPattern(String parent, Pattern pattern) throws RepositoryException {
        List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
        try {
            repository.getDir(parent, -1, false, entries);
            Collections.sort(entries, FileFirstSVNDirEntryComparator.C);
            for (SVNDirEntry entry : entries) {
                if (entry.getKind() == FILE) {
                    if (pattern.matcher(entry.getURL().toString()).matches()) {
                        return true;
                    }
                }
                else if (entry.getKind() == DIR) {
                    boolean exists = existsFileByPattern(
                        (StringUtils.isEmpty(parent) ? "" : parent + "/") + entry.getName(), pattern);
                    if (exists) {
                        return true;
                    }
                }
            }
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
        return false;
    }

    public FileInfo getFile(String path) throws FileNotFoundException, RepositoryException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            repository.getFile(path, -1, null, out);
            FileInfo result = new FileInfo();
            String url = repository.getLocation().toString();
            url += url.endsWith("/") ? path : "/".concat(path);
            result.setUrl(url);
            result.setContent(new String(out.toByteArray(), "UTF-8"));
            return result;
        } catch (SVNException e) {
            throw new RepositoryException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public long getLatestRevision() {
        try {
            return repository.getLatestRevision();
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class FileFirstSVNDirEntryComparator implements Comparator<SVNDirEntry> {

        protected static final FileFirstSVNDirEntryComparator C = new FileFirstSVNDirEntryComparator();

        private FileFirstSVNDirEntryComparator() {}

        public int compare(SVNDirEntry l, SVNDirEntry r) {
            if (l.getKind() == FILE) {
                if (r.getKind() == FILE) {
                    return l.compareTo(r);
                }
                return -1;
            }
            if (r.getKind() == FILE) {
                return 1;
            }
            return l.compareTo(r);
        }
        
    }
}
