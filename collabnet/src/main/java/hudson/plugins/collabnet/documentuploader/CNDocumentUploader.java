package hudson.plugins.collabnet.documentuploader;


import com.collabnet.ce.soap50.webservices.docman.DocumentSoapDO;
import com.collabnet.ce.webservices.CollabNetApp;
import com.collabnet.ce.webservices.DocumentApp;
import com.collabnet.ce.webservices.FileStorageApp;

import hudson.Extension;
import hudson.Launcher;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import hudson.plugins.collabnet.share.TeamForgeShare;
import hudson.plugins.collabnet.util.CNFormFieldValidator;
import hudson.plugins.collabnet.util.CNHudsonUtil;
import hudson.plugins.collabnet.util.ComboBoxUpdater;
import hudson.plugins.collabnet.util.CommonUtil;

import java.io.IOException;
import java.io.File;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Hudson plugin to upload the Hudson build log 
 * to the CollabNet Documents.
 */
public class CNDocumentUploader extends Notifier {
    private static Logger logger = Logger.getLogger("CNDocumentUploader");
    private static final String IMAGE_URL = "/plugin/collabnet/images/48x48/";
	
    // listener is used for logging and will only be
    // set at the beginning of perform.
    private transient BuildListener listener = null;

    // collabNet object
    private transient CollabNetApp cna = null;

    // Variables from the form
    private boolean override_auth = true;
    private String url;
    private String username;
    private String password;
    private String project;
    private String uploadPath;
    private String description;
    private String[] file_patterns;
    private boolean includeBuildLog;    

    private transient static TeamForgeShare.TeamForgeShareDescriptor 
        shareDescriptor = null;

    /**
     * Creates a new CNDocumentUploader object.
     *
     * @param url for the CollabNet server, i.e. http://forge.collab.net
     * @param username used to log into the CollabNet server.
     * @param password for the logging-in user.
     * @param project where the build log will be uploaded.
     * @param uploadPath on the CollabNet server, where the build log should
     *                   be uploaded.
     * @param description
     * @param filePatterns
     * @param includeBuildLog
     * @param override_auth
     */
    public CNDocumentUploader(String url, String username, String password, 
                              String project, String uploadPath, 
                              String description, String[] filePatterns,
                              boolean includeBuildLog, boolean override_auth) {
        this.url = CNHudsonUtil.sanitizeCollabNetUrl(url);
        this.username = username;
        this.password = password;
        this.project = project;
        this.uploadPath = uploadPath;
        this.description = description;
        this.file_patterns = filePatterns;
        this.includeBuildLog = includeBuildLog;
        this.override_auth = override_auth;
    }

    /**
     * Setting the listener allows logging to work.
     *
     * @param listener passed into the perform method.
     */
    private void setupLogging(BuildListener listener) {
        this.listener = listener;
    }

    /**
     * Log a message to the console.  Logging will only work once the 
     * listener is set. Otherwise, it will fail (silently).
     *
     * @param message A string to print to the console.
     */
    private void log(String message) {
        if (this.listener != null) {
            message = "CollabNet Document Uploader: " + message;
            this.listener.getLogger().println(message);
        }
    }
    
    /**
     * Convenience method to log RemoteExceptions.  
     *
     * @param methodName in progress on when this exception occurred.
     * @param re The RemoteException that was thrown.
     */
    private void log(String methodName, RemoteException re) {
        CommonUtil.logRE(logger, methodName, re);
    }

    /**
     * @return whether or not auth is overriden
     */
    public boolean overrideAuth() {
        return this.override_auth;
    }

    /**
     * @return the url for the CollabNet server.
     */
    public String getCollabNetUrl() {
        if (this.overrideAuth()) {
            return this.url;
        } else {
            return getTeamForgeShareDescriptor().getCollabNetUrl();
        }
    }

    /**
     * @return the username used for logging in.
     */
    public String getUsername() {
        if (this.overrideAuth()) {
            return this.username;            
        } else {
            return getTeamForgeShareDescriptor().getUsername();
        }
    }

    /**
     * @return the password used for logging in.
     */
    public String getPassword() {
        if (this.overrideAuth()) {
            return this.password;
        } else {
            return getTeamForgeShareDescriptor().getPassword();
        }
    }

    /**
     * @return the project where the build log is uploaded.
     */
    public String getProject() {
        return this.project;
    }

    /**
     * @return the path where the build log is uploaded.
     */
    public String getUploadPath() {
        return this.uploadPath;
    }

    /**
     * @return the description of the uploaded files.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * @return the ant-style file patterns.
     */
    public String[] getFilePatterns() {
        if (this.file_patterns != null) {
            return this.file_patterns;
        } else {
            return new String[0];
        }
    }

    /**
     * @return true if the build log should be uploaded.
     */
    public boolean includeBuildLog() {
        return this.includeBuildLog;
    }

    /**
     * @return the TeamForge share descriptor.
     */
    public static TeamForgeShare.TeamForgeShareDescriptor 
        getTeamForgeShareDescriptor() {
        if (shareDescriptor == null) {
            shareDescriptor = TeamForgeShare.getTeamForgeShareDescriptor();
        }
        return shareDescriptor;
    }    

    /**
     * @return the list of all possible projects, given the login data.
     */
    public String[] getProjects() {
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(this.getCollabNetUrl(),
                                                        this.getUsername(), 
                                                        this.getPassword());
        Collection<String> projects = ComboBoxUpdater.ProjectsUpdater
            .getProjectList(cna);
        CNHudsonUtil.logoff(cna);
        return projects.toArray(new String[0]);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * The function does the work of uploading the build log.
     *
     * @param build current Hudson build.
     * @param launcher unused.
     * @param listener receives events that happen during a build.  We use it 
     *                 for logging.
     * @return true if successful, false if a critical error occurred.
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, 
                           BuildListener listener) throws IOException, InterruptedException {
        this.setupLogging(listener);
        this.cna = CNHudsonUtil.getCollabNetApp(this.getCollabNetUrl(), 
                                                this.getUsername(), 
                                                this.getPassword());
        if (this.cna == null) {
            this.log("Critical Error: login to " + this.getCollabNetUrl() + 
                     " failed.  Setting build status to UNSTABLE (or worse).");
            Result previousBuildStatus = build.getResult();
            build.setResult(previousBuildStatus.combine(Result.UNSTABLE));
            build.addAction(this.createAction(0, null));
            return false;
        }
        String projectId = this.getProjectId();
        if (projectId == null) {
            this.log("Critical Error: Unable to find project '" + 
                     this.getProject() + "'.  " + 
                     "Setting build status to UNSTABLE (or worse).");
            Result previousBuildStatus = build.getResult();
            build.setResult(previousBuildStatus.combine(Result.UNSTABLE));
            build.addAction(this.createAction(0, null));
            this.logoff();
            return false;
        }
        DocumentApp da = new DocumentApp(this.cna);
        String folderId = null;
        String path = this.getInterpreted(build, this.getUploadPath());
        try {
            folderId = da.findOrCreatePath(projectId, path);
        } catch (RemoteException re) {
            this.log("findOrCreatePath", re);
            // if this fails, cannot continue
            this.log("Critical Error: Unable to create a path for '" + 
                     path + "'.  Setting build status to " +
                     "UNSTABLE (or worse).");
            Result previousBuildStatus = build.getResult();
            build.setResult(previousBuildStatus.combine(Result.UNSTABLE));
            build.addAction(this.createAction(0, folderId));
            this.logoff();
            return false;
        }
        int numUploaded = this.uploadFiles(folderId, build);
        build.addAction(this.createAction(numUploaded, folderId));
        try {
            this.cna.logoff();
        } catch (RemoteException re) {
            this.log("logoff", re);
        }
        return true;
    }

    private Action createAction(int numUploaded, String folderId) {
        String displaymsg = "Download from CollabNet Documents";
        return new CnduResultAction(displaymsg, 
                                    IMAGE_URL + "cn-icon.gif", 
                                    "console", 
                                    this.getFolderUrl(folderId), 
                                    numUploaded);
    }

    /**
     * @param folderId
     * @return the url pointing to this folder.
     */
    private String getFolderUrl(String folderId) {
        String path = null;
        if (folderId != null) {
            try {
                DocumentApp da = new DocumentApp(this.cna);
                path = da.getFolderPath(folderId);
            } catch (RemoteException re) {
                this.log("getFolderUrl", re);
            }
        }
        if (path == null) {
            return this.getCollabNetUrl();
        } else {
            return this.getCollabNetUrl() + "/sf/docman/do/listDocuments/" 
                + path;
        }
    }

    /**
     * Upload files matching the file patterns to the Document Service.
     *
     * @param folderId folder where the files should be uploaded.
     * @param build the current Hudson build.
     * @return the number of files successfully uploaded.
     */
    public int uploadFiles(String folderId, AbstractBuild<?, ?> build)
            throws IOException, InterruptedException {
        int numUploaded = 0;
        String path = this.getInterpreted(build, this.getUploadPath());
        this.log("Uploading files to project '" + this.getProject() + 
                 "', folder '" + path + "' on host '" + 
                 this.getCollabNetUrl() + "' as user '" + this.getUsername() 
                 + "'.");
        for (String uninterp_fp : this.getFilePatterns()) {
            String file_pattern = "";
            try {
                file_pattern = getInterpreted(build, uninterp_fp); 
            } catch (IllegalArgumentException e) {
                this.log("File pattern " + uninterp_fp + " contained a bad "
                         + "env var.  Skipping.");
                continue;
            }
            if (file_pattern.equals("")) {
                // skip empty fields
                continue;
            }
            this.log("Upload files matching " + file_pattern + ":");
            for (FilePath uploadFilePath : this.getFilePaths(build, 
                                                             file_pattern)) {
                String fileId = this.uploadFile(uploadFilePath);
                if (fileId == null) {
                    this.log("Failed to upload " + uploadFilePath.getName() 
                             + ".");
                    continue;
                }
                String docId = null;
                try {
                    docId = this.updateOrCreateDoc(folderId, fileId, 
                                                   uploadFilePath.getName(),
                                                   CNDocumentUploader.
                                                   getMimeType(uploadFilePath),
                                                   build);
                    this.log("Uploaded " + uploadFilePath.getName() + " -> " 
                             + this.getDocUrl(docId));
                    numUploaded++;
                } catch (RemoteException re) {
                    this.log("updateOrCreateDoc", re);
                }
            }
        }
        if (this.includeBuildLog()) {
            String fileId = this.uploadBuildLog(build);
            if (fileId == null) {
                    this.log("Failed to upload " + build.getLogFile().getName() 
                             + ".");
            }
            else {
                String docId = null;
                try {
                    docId = this.updateOrCreateDoc(folderId, fileId, 
                                                   build.getLogFile().getName(),
                                                   CNDocumentUploader.
                                                   getMimeType(build.
                                                               getLogFile()),
                                                   build);
                    this.log("Uploaded " + build.getLogFile().getName() 
                             + " -> " + this.getDocUrl(docId));
                    numUploaded++;
                } catch (RemoteException re) {
                    this.log("updateOrCreateDoc", re);
                }
            }
        }
        return numUploaded;
    }

    /**
     * Get the document's URL.
     *
     * @param docId document's id.
     * @return an absolute URL to the document.
     */
    private String getDocUrl(String docId) {
        return this.getCollabNetUrl() + "/sf/go/" + docId;
    }

    /**
     * Return the filepaths in the workspace which match the pattern.
     *
     * @param build The hudson build.
     * @param pattern An ant-style pattern.
     * @return an array of FilePaths which match this pattern in the 
     *         hudson workspace.
     */
    private FilePath[] getFilePaths(AbstractBuild<?, ?> build, 
                                    String pattern) {
        FilePath workspace = build.getWorkspace();
        FilePath[] uploadFilePaths = new FilePath[0];
        try {
            uploadFilePaths = workspace.list(pattern);
        } catch (IOException ioe) {
            this.log("Could not list workspace due to IOException: " 
                     + ioe.getMessage());
        } catch (InterruptedException ie) {
            this.log("Could not list workspace due to " +
                     "InterruptedException: " + ie.getMessage());
        }
        return uploadFilePaths;
    }

    /**
     * If a document exists under this folder with the fileName, 
     * increment it's version and update with the new fileId.
     * Otherwise, create a new document.
     *
     * @param folderId of the folder where the document will be 
     *                 created/updated. 
     * @param fileId of the already upload build log.
     * @param fileName name of the uploaded file.
     * @param mimeType of the uploaded file.
     * @param build the current Hudson build.
     * @return the docId associated with the new/updated document.
     * @throws RemoteException
     */
    private String updateOrCreateDoc(String folderId, String fileId, 
                                     String fileName, String mimeType, 
                                     AbstractBuild<?, ?> build) 
        throws RemoteException, IOException, InterruptedException {
        DocumentApp da = new DocumentApp(this.cna);
        String docId = da.findDocumentId(folderId, fileName);
        if (docId != null) {
            da.updateDoc(docId, fileId);
        } else {
            DocumentSoapDO document = da.
                createDocument(folderId, fileName, 
                               this.getInterpreted(build, 
                                                   this.getDescription()), 
                               null, "final", false, fileName, 
                               mimeType, fileId, null, null);
            docId = document.getId();
        }
        return docId;
    }

    /**
     * Get the mimetype for the file.
     *
     * @param filePath The filePath to return the mimetype for.
     * @return the string representing the mimetype of the file.
     */
    public static String getMimeType(FilePath filePath) {
        String mimeType = "text/plain";
        try {
            mimeType = filePath.act(new FileCallable<String>() {
                @Override
                public String invoke(File f, VirtualChannel channel) 
                throws IOException, RemoteException {
                    if (f.getName().endsWith("log")) {
                        return "text/plain";
                    }
                    return new MimetypesFileTypeMap().getContentType(f);
                }});
        } catch (IOException ioe) { // ignore exceptions
        } catch (InterruptedException ie) {}
        return mimeType;
    }

    /**
     * Get the mimetype for the file.
     *
     * @param f The file to return the mimetype for.
     * @return the string representing the mimetype of the file.
     */
    public static String getMimeType(File f) {
        if (f.getName().endsWith("log")) {
            return "text/plain";
        }
        return new MimetypesFileTypeMap().getContentType(f);
    }

    /**
     * Upload the build log to the collabnet server.
     *
     * @param build the current Hudson build.
     * @return the id associated with the file upload.
     */
    private String uploadBuildLog(AbstractBuild <?, ?> build) {
        if (this.cna == null) {
            this.log("Cannot call updateSucceedingBuild, not logged in!");
            return null;
        }
        String id = null;
        FileStorageApp sfsa = new FileStorageApp(this.cna);
        try {
            id = sfsa.uploadFile(build.getLogFile());
        } catch (RemoteException re) {
            this.log("uploadBuildLog", re);
        }
        return id;
    }

    /**
     * Upload the build log to the collabnet server.
     *
     * @param filePath the path of file to upload
     * @return the id associated with the file upload.
     */
    private String uploadFile(FilePath filePath) {
        if (this.cna == null) {
            this.log("Cannot call uploadFile, not logged in!");
            return null;
        }
        String id = null;
        final FileStorageApp sfsa = new FileStorageApp(this.cna);
        try {
            id = filePath.act(new FileCallable<String>() {
                @Override
                public String invoke(File f, VirtualChannel channel) 
                throws IOException, RemoteException {      
                    return sfsa.uploadFile(f);
                }
            });
        } catch (RemoteException re) {
            this.log("upload file", re);
        } catch (IOException ioe) {
            this.log("Could not upload file due to IOException: " 
                     + ioe.getMessage());
        } catch (InterruptedException ie) {
            this.log("Could not upload file due to " +
                     "InterruptedException: " + ie.getMessage());
        }
        return id;
    }

    /**
     * Log out of the collabnet server.   Invalidates the cna object.
     */
    public void logoff() {
        CNHudsonUtil.logoff(this.cna);
        this.cna = null;        
    }

    /**
     * Get the project id for the project name.
     * 
     * @return the matching project id, or null if none is found.
     */
    public String getProjectId() {
        if (this.cna == null) {
            this.log("Cannot getProjectId, not logged in!");
            return null;
        }
        return CNHudsonUtil.getProjectId(this.cna, this.getProject());
    }

    /**
     * Translates a string that may contain  build vars like ${BUILD_VAR} to
     * a string with those vars interpreted.
     * 
     * @param build the Hudson build.
     * @param str the string to be interpreted.
     * @return the interpreted string.
     * @throws IllegalArgumentException if the env var is not found.
     */
    private String getInterpreted(AbstractBuild<?, ?> build, String str)
            throws IOException, InterruptedException {
        try {
            return CommonUtil.getInterpreted(build.getEnvironment(TaskListener.NULL), str);
        } catch (IllegalArgumentException iae) {
            this.log(iae.getMessage());
            throw iae;
        }
    }

    /**
     * The CNDocumentUploader Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(CNDocumentUploader.class);
        }

        @Override
        /**
         * Implementation of the abstract isApplicable method from
         * BuildStepDescriptor.
         */
         public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * @return string to display for configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "CollabNet Document Uploader";
        }

        /**
         * @return the root for the document uploader's resources.
         */
        public static String getHelpUrl() {
            return "/plugin/collabnet/documentuploader";
        }

        /**
         * @return a relative url to the main help file.
         */
        @Override
        public String getHelpFile() {
            return getHelpUrl() +"/help-main.html";
        }

        /**
         * @return true if there is auth data that can be inherited.
         */
        public boolean canInheritAuth() {
            return getTeamForgeShareDescriptor().useGlobal();
        }    

        /**
         * Creates a new instance of {@link CNDocumentUploader} from a 
         * submitted form.
         *
         * @param req config page parameters.
         * @return new CNDocumentUploader object, instantiated from the 
         *         configuration form vars.
         * @throws FormException
         */
        @Override
        public CNDocumentUploader newInstance(StaplerRequest req, 
                                              JSONObject formData) 
            throws FormException {
            boolean override_auth = false;
            String username = null;
            String password = null;
            String collabneturl = null;
            if (formData.has("override_auth")) {
                override_auth = true;
                Object authObject = formData.get("override_auth");
                if (authObject instanceof JSONObject) {
                    username = (String)((JSONObject) authObject)
                        .get("username");
                    password = (String)((JSONObject) authObject)
                        .get("password");
                    collabneturl = (String)((JSONObject) authObject)
                        .get("collabneturl");
                } else if (authObject.equals(Boolean.TRUE)) {
                    username = (String) formData.get("username");
                    password = (String) formData.get("password");
                    collabneturl = (String) formData.get("collabneturl");
                } else {
                    override_auth = false;
                }
            } else if (!this.canInheritAuth()) {
                override_auth = true;
                username = (String)formData.get("username");
                password = (String)formData.get("password");
                collabneturl = (String)formData.get("collabneturl");
            }
            Object fileData = formData.get("files");
            JSONObject[] jpats;
            if (fileData instanceof JSONArray) {
                JSONArray patData = (JSONArray) fileData;
                jpats = (JSONObject []) JSONArray.toArray(patData, 
                                                          JSONObject.class);
            } else if (fileData instanceof JSONObject) {
                jpats = new JSONObject[1];
                jpats[0] = (JSONObject) fileData;
            } else {
                jpats = new JSONObject[0];
            }
            String[] patterns = new String[jpats.length];
            for (int i = 0; i < jpats.length; i++) {
                patterns[i] = (String) jpats[i].get("file");
            }
            String path = (String)formData.get("upload_path");
            path = path.replaceAll("/+", "/");
            path = CommonUtil.stripSlashes(path);
            return new CNDocumentUploader(collabneturl,
                                          username,
                                          password,
                                          (String)formData.get("project"),
                                          path,
                                          (String)formData.get("description"),
                                          patterns,
                                          CommonUtil.getBoolean("buildlog", 
                                                                formData),
                                          override_auth);
        }

        /**
         * Form validation for the CollabNet URL.
         *
         * @param value url
         */
        public FormValidation doCollabNetUrlCheck(@QueryParameter String value) {
            return CNFormFieldValidator.soapUrlCheck(value);
        }

        /**
         * Form validation for username.
         *
         * @param value to check
         * @param name of field
         */
        public FormValidation doRequiredCheck(@QueryParameter String value,
                @QueryParameter String name) {
            return CNFormFieldValidator.requiredCheck(value, name);
        }
        
        /**
         * Check that a password is present and allows login.
         *
         * @param password
         */
        public FormValidation doPasswordCheck(StaplerRequest req) {
            return CNFormFieldValidator.loginCheck(req);
        }
        
        /**
         * Form validation for the project field.
         *
         * @param req contains parameters from 
         *            the config.jelly.
         * @param rsp contains http response data (unused).
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doProjectCheck(StaplerRequest req) {
            return CNFormFieldValidator.projectCheck(req);
        }

        /**
         * Form validation for upload path.
         *
         * @param req StaplerRequest which contains parameters from 
         *            the config.jelly.
         * @param rsp contains http response data (unused).
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doPathCheck(StaplerRequest req) throws IOException {
            return CNFormFieldValidator.documentPathCheck(req);
        }

        /**
         * Form validation for the file patterns.
         *
         * @param req StaplerRequest which contains parameters from 
         *            the config.jelly.
         * @param rsp contains http response data (unused).
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doUnRequiredInterpretedCheck(
                @QueryParameter String value, @QueryParameter String name) {
            return CNFormFieldValidator.unrequiredInterpretedCheck(value, name);
        }

        /**
         * Updates the list of projects in the combo box.  Expects the
         * StaplerRequest to contain url, username, and password.
         *
         * @param req contains parameters from 
         *            the config.jelly.
         * @param rsp http response data.
         * @throws IOException
         */
        public void doGetProjects(StaplerRequest req, StaplerResponse rsp) 
            throws IOException {
            new ComboBoxUpdater.ProjectsUpdater(req, rsp).update();
        }
    }
}
