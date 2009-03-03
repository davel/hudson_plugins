package hudson.scm;

import hudson.remoting.Which;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.internal.wc.SVNExternal;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

/**
 * Just prints out the progress of svn update/checkout operation in a way similar to
 * the svn CLI.
 *
 * This code also records all the referenced external locations.
 */
final class SubversionReleaseUpdateEventHandler extends SubversionEventHandlerImpl {

    /**
     * External urls that are fetched through svn:externals.
     * We add to this collection as we find them.
     */
    private final List<SubversionReleaseSCM.External> externals;
    /**
     * Relative path from the workspace root to the module root. 
     */
    private final String modulePath;
    
    public SubversionReleaseUpdateEventHandler(PrintStream out, List<SubversionReleaseSCM.External> externals, File moduleDir, String modulePath) {
        super(out,moduleDir);
        this.externals = externals;
        this.modulePath = modulePath;
    }

    public void handleEvent(SVNEvent event, double progress) {
        File file = event.getFile();
        String path = null;
        if (file != null) {
            path = getRelativePath(file);
            path = getLocalPath(path);
        }

        /*
         * Gets the current action. An action is represented by SVNEventAction.
         * In case of an update an  action  can  be  determined  via  comparing
         * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants.
         */
        SVNEventAction action = event.getAction();
        if (action == SVNEventAction.UPDATE_EXTERNAL) {
            // for externals definitions
            SVNExternal ext = event.getExternalInfo();
            if(ext==null) {
                // prepare for the situation where the user created their own svnkit
                URL jarFile = null;
                try {
                    jarFile = Which.jarURL(SVNEvent.class);
                } catch (IOException e) {
                    // ignore this failure
                }
                out.println("AssertionError: appears to be using unpatched svnkit at "+ jarFile);
            } else {
                out.println(Messages.SubversionUpdateEventHandler_FetchExternal(
                        ext.getResolvedURL(), ext.getRevision().getNumber(), event.getFile()));
                //#1539 - an external inside an external needs to have the path appended 
                externals.add(new SubversionReleaseSCM.External(modulePath + "/" + path.substring(0
                		,path.length() - ext.getPath().length())
                		,ext));
            }
            return;
        }

        super.handleEvent(event,progress);
    }

    public void checkCancelled() throws SVNCancelException {
        if(Thread.interrupted())
            throw new SVNCancelException();
    }
}