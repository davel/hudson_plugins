package hudson.plugins.jswidgets;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.scm.EditType;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class implements the JS widgets pages for a build.
 * 
 * @author mfriedenhagen
 */
public class JsBuildAction extends JsBaseAction {
    
    /** Our logger. */
    private static final Logger LOG = Logger.getLogger(JsBuildAction.class.getName());


    /** the build this action acts on. */
    private final AbstractBuild<?, ?> build;

    /** describe type of edits. */
    private static final HashMap<EditType, String> EDIT_TYPE_SYMBOLS = new HashMap<EditType, String>();

    static {
        EDIT_TYPE_SYMBOLS.put(EditType.ADD, "+");
        EDIT_TYPE_SYMBOLS.put(EditType.DELETE, "-");
        EDIT_TYPE_SYMBOLS.put(EditType.EDIT, "#");
    }

    /**
     * @param build
     *            this action acts on.
     */
    public JsBuildAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return JsConsts.URLNAME;
    }

    /**
     * Returns the build.
     * 
     * @return the build.
     */
    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    /**
     * Returns the project.
     * 
     * @return the project.
     */
    public AbstractProject<?, ?> getProject() {
        return build.getProject();
    }

    /**
     * Returns the Name of the {@link Node} where the {@link AbstractBuild} happened. If the {@link Node} is deleted
     * meanwhile it returns <tt>UNKNOWN</tt>.
     * 
     * @return name of the node.
     */
    public String getBuiltOn() {
        final Node builtOnNode = build.getBuiltOn();
        if (builtOnNode == null) {
            return "UNKNOWN";
        } else {
            return builtOnNode.getNodeDescription();
        }
    }

    /**
     * Returns a representation for affected files in <tt>entry</tt> prefixing them with the {@link EditType} of the
     * change. Some {@link SCM}s do not support {@link Entry#getAffectedFiles()}, so do fall back to
     * {@link Entry#getAffectedPaths()} in this case.
     * 
     * @param entry
     *            changeset.
     * @return collection of {@link EditType} prefixed paths.
     */
    public Collection<String> getChangeSetEntries(Entry entry) {
        final Collection<? extends AffectedFile> affectedFiles;
        try {
            affectedFiles = entry.getAffectedFiles();
        } catch (UnsupportedOperationException e) {
            LOG.warning("Got " + e + ", falling back to getAffectedPaths");
            return entry.getAffectedPaths();
        }
        final ArrayList<String> entries = new ArrayList<String>();
        for (final AffectedFile affectedFile : affectedFiles) {
            entries.add(EDIT_TYPE_SYMBOLS.get(affectedFile.getEditType()) + affectedFile.getPath());
        }
        return entries;
    }
}
