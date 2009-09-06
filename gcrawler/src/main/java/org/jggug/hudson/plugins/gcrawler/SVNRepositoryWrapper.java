package org.jggug.hudson.plugins.gcrawler;

import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.getFile;
import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.joinAsPath;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

public class SVNRepositoryWrapper {

    private static final int DEFAULT_MAX_DEPTH = 3;

    static {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    private int maxDepth;

    private String svnroot;

    private SVNRepository repository;

    public SVNRepositoryWrapper(String svnroot) {
        this(svnroot, DEFAULT_MAX_DEPTH);
    }

    public SVNRepositoryWrapper(String svnroot, int maxDepth) {
        this.maxDepth = maxDepth;
        this.svnroot = svnroot;
        try {
            SVNURL svnUrl = SVNURL.parseURIDecoded(svnroot);
            repository = SVNClientManager.newInstance().createRepository(svnUrl, true);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (repository != null) {
            repository.closeSession();
        }
    }

    public SVNFIleInfo findFile(String fileName) throws FileNotFoundException {
        try {
            return findFile("trunk", fileName, 0);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    private SVNFIleInfo findFile(String path, String targetFileName, int depth) throws FileNotFoundException, SVNException {
        if (depth >= maxDepth) {
            throw new FileNotFoundException();
        }

        List<SVNDirEntry> children = new ArrayList<SVNDirEntry>();
        repository.getDir(path, -1, null, children);

        try {
            return findFileFromDirectory(path, targetFileName, children);
        } catch (FileNotFoundException ignore) {}

        // find recurse
        for (Iterator<SVNDirEntry> it = children.iterator(); it.hasNext();) {
            SVNDirEntry entry = it.next();
            String fullPath = joinAsPath(path, entry.getRelativePath());
            if (entry.getKind() == SVNNodeKind.DIR) {
                try {
                    return findFile(fullPath, targetFileName, ++depth);
                } catch (FileNotFoundException ignore) {}
            }
        }

        // not found.
        throw new FileNotFoundException();
    }

    public List<String> listFiles(String dir) {
        return null;
    }

    private SVNFIleInfo findFileFromDirectory(String pathPrefix, String targetFileName, List<SVNDirEntry> children) throws FileNotFoundException {
        for (Iterator<SVNDirEntry> it = children.iterator(); it.hasNext();) {
            String fullPath = joinAsPath(pathPrefix, it.next().getRelativePath());
            try {
                return getFile(joinAsPath(svnroot, fullPath, targetFileName));
            } catch (FileNotFoundException ignore) {}
        }
        throw new FileNotFoundException();
    }
}
