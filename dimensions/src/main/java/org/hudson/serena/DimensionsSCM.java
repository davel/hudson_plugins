package org.hudson.serena;

import org.hudson.serena.model.Dimension10Installation;
import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.DimensionsRelatedObject;
import com.serena.dmclient.api.DimensionsResult;
import com.serena.dmclient.api.DownloadCommandDetails;
import com.serena.dmclient.api.Filter;
import com.serena.dmclient.api.Project;
import com.serena.dmclient.api.SystemAttributes;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.SCM;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.xml.sax.SAXException;

/**
 * Hudson SCM implementation able to connect to a Serena Dimensions 10
 * server.
 *
 * @author Jose Noheda [jose.noheda@gmail.com]
 */
public class DimensionsSCM extends SCM implements Serializable {

    private static final long serialVersionUID = 2061202506815349346L;

    private static final Logger LOGGER = Logger.getLogger(DimensionsSCM.class.getName());

    private String product;
    private String project;
    private String subfolder;
    private boolean canUseUpdate;
    private Dimension10Installation installation;

    @DataBoundConstructor
    public DimensionsSCM(String server, String product, String project, String subfolder, boolean canUseUpdate) {
        setInstallation(getDescriptor().getInstallation(server));
        setProduct(product);
        setProject(project);
        setSubfolder(subfolder);
        setCanUseUpdate(canUseUpdate);
    }

    public final String getProduct() {
        return product;
    }

    public final void setProduct(String product) {
        this.product = product;
    }

    public final String getProject() {
        return project;
    }

    public final void setProject(String project) {
        this.project = project;
    }

    public final String getSubfolder() {
        return subfolder;
    }

    public final void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }

    public final Dimension10Installation getInstallation() {
        return installation;
    }

    public final void setInstallation(Dimension10Installation installation) {
        this.installation = installation;
    }

    public final boolean isCanUseUpdate() {
        return canUseUpdate;
    }

    public final void setCanUseUpdate(boolean canUseUpdate) {
        this.canUseUpdate = canUseUpdate;
    }

    @Override public FilePath getModuleRoot(FilePath workspace) {
        String branch = subfolder;
        if ((branch == null) || (branch.trim().length() == 0)) {
            branch = project;
        }
        return new FilePath(workspace, branch);
    }
    @Override public boolean pollChanges(AbstractProject build, Launcher launcher, FilePath workspace, TaskListener arg3) throws IOException, InterruptedException {
        FilePath projectPath = getModuleRoot(workspace);
        if (projectPath.exists()) {
            Date lastModified = new Date(projectPath.lastModified());
            DimensionsConnection conn = ConnectionManager.getConnection(getInstallation());
            conn.initialise();
            Project dimensionsProject = conn.getObjectFactory().getProject(product + ":" + project);
            Filter filter = new Filter();
            try {
                filter.criteria().add(new Filter.Criterion(SystemAttributes.IS_LATEST_REV, true, Filter.Criterion.EQUALS));
                String lastModifiedFormat = new SimpleDateFormat("dd-MMM-yyyy").format(lastModified);
                filter.criteria().add(new Filter.Criterion(SystemAttributes.LAST_UPDATED_DATE, lastModifiedFormat, Filter.Criterion.GREATER_EQUAL));
                if (nullify(subfolder) != null) {
                    filter.criteria().add(new Filter.Criterion(SystemAttributes.FULL_PATH_NAME, subfolder + "/%", Filter.Criterion.EQUALS));
                }
                List<DimensionsRelatedObject> items = dimensionsProject.getChildItems(filter);
                return (items != null) && (items.size() > 0);
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override public ChangeLogParser createChangeLogParser() {
        return new ChangeLogParser() {

            @Override
            public ChangeLogSet<? extends Entry> parse(final AbstractBuild build, final File changelogFile) throws IOException, SAXException {
                return new DimensionsChangeLogSet(build, changelogFile);
            }

        };
    }

    @Override public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        FilePath projectPath = getModuleRoot(workspace);
        if (canUseUpdate && isUpdatable(projectPath)) {
            LOGGER.info("Updating [" + product + ":" + project + (subfolder == null ? "/" + subfolder : "") + "] from Dimensions server to [" + projectPath.toURI() + "]");
            return doUpdate(projectPath, changelogFile);
        }
        return doCheckout(projectPath, changelogFile);
    }

    private boolean isUpdatable(FilePath projectPath) {
        boolean updatable = false;
        try {
            updatable = projectPath.exists() && projectPath.isDirectory();
            if (updatable) {
                FilePath dimensionsMetadata = new FilePath(projectPath, ".metadata");
                updatable = dimensionsMetadata.exists() && dimensionsMetadata.isDirectory();
            }
        } catch (Exception ex) {
            LOGGER.fine("[" + projectPath + "] is not updatable: " + ex.getMessage());
            updatable = false;
        }
        return updatable;
    }

    private boolean doCheckout(FilePath projectPath, File changelogFile) {
        try {
            if (projectPath.exists()) {
                LOGGER.info("Cleaning [" + projectPath.toURI() + "]...");
                projectPath.deleteRecursive();
            }
            projectPath.getParent().mkdirs();
            LOGGER.info("Checking out [" + product + ":" + project + "/" + subfolder + "] from Dimensions server to [" + projectPath.toURI() + "]");
            return doUpdate(projectPath, changelogFile);
        } catch (Exception ex) {
            LOGGER.warning("Unexpected exception preparing checkout: " + ex.getMessage());
            return false;
        }
    }

    private boolean doUpdate(FilePath projectPath, File changelogFile) {
        try {
            DimensionsConnection conn = ConnectionManager.getConnection(getInstallation());
            conn.initialise();
            DownloadCommandDetails downloadDetails = new DownloadCommandDetails();
            downloadDetails.setRecursive(true);
            downloadDetails.setUserDirectory(projectPath.getParent().toURI().toString().replace("file:/", ""));
            if ((subfolder != null) && (subfolder.trim().length() > 0)) {
                downloadDetails.setDirectory(subfolder);
            }
            DimensionsResult result = conn.getObjectFactory().getProject(product + ":" + project).download(downloadDetails);
            projectPath.touch(new Date().getTime());
            generateLogFile(result.getMessage(), changelogFile);
            return true;
        } catch (Exception ex) {
            LOGGER.warning("Unexpected exception processing checkout: " + ex.getMessage());
            try {
                projectPath.deleteRecursive();
            } catch (Exception dex) {
                LOGGER.warning("Could not delete [" + projectPath + "]: " + dex.getMessage());
            }
            return false;
        }

    }

    protected void generateLogFile(String contents, File changelogFile) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(changelogFile);
            writer.write(contents);
            writer.flush();
        } catch (IOException ioe) {
            LOGGER.warning("Unexpected exception writing Dimensions change log file: " + ioe.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
                LOGGER.warning("Could not close Dimensions change log after writing: " + ioe.getMessage());
            }
        }
    }

    @Override public DimensionsDescriptor getDescriptor() {
        return DimensionsDescriptor.DESCRIPTOR;
    }

}
