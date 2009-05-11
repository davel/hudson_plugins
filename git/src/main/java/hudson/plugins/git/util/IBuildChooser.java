package hudson.plugins.git.util;

import hudson.model.Action;
import hudson.model.Result;
import hudson.plugins.git.GitException;
import hudson.plugins.git.Revision;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.spearce.jgit.lib.ObjectId;

/**
 * Interface defining an API to choose which revisions ought to be
 * considered for building.
 * 
 * @author magnayn
 *
 */
public interface IBuildChooser
{
    /**
     * Get a list of revisions that are candidates to be built.
     * May be an empty set.
     * @return
     * @throws IOException 
     * @throws GitException 
     */
    Collection<Revision> getCandidateRevisions() throws GitException, IOException;

    /**
     * Report back whether a revision built was successful or not.
     * @param revision
     * @param success
     */
    Build revisionBuilt(Revision revision, int buildNumber, Result result);
    
    /**
     * What was the last SHA1 that a named branch was built with?
     * @param branch
     * @return ObjectId, or NULL 
     */
    //Build getLastBuiltRevisionOfBranch(String branch);
    
    /**
     * What was the last revision to be built?
     * @return
     */
    //public Revision getLastBuiltRevision();

    /**
     * Get data to be persisted.
     * @return
     */
    Action getData();
}
