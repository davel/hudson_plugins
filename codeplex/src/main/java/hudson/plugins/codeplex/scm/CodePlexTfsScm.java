package hudson.plugins.codeplex.scm;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.codeplex.soap.ProjectInfoServiceLocator;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.Project;
import hudson.model.TaskListener;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.codeplex.PluginImpl;
import hudson.plugins.codeplex.browsers.CodePlexTfsBrowser;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.Scrambler;

@SuppressWarnings("unchecked")
public class CodePlexTfsScm extends SCM {

    private transient SCM configuredScm;
    private transient CodePlexTfsBrowser tfsBrowser;
    
    private final String path;
    private final String userName;
    private final String userPassword;
    
    @DataBoundConstructor
    public CodePlexTfsScm(String path, String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = Scrambler.scramble(userPassword);

        this.path = path;
        if (Util.fixEmptyAndTrim(path) == null) {
            path = ".";
        }
    }
    
    public String getPath() {
        return path;
    }

    public String getUserPassword() {
        return Scrambler.descramble(userPassword);
    }

    public String getUserName() {
        return userName;
    }

    public SCM getScm() {
        if (configuredScm == null) {
            try {
                for (AbstractProject<?, ?> project : Hudson.getInstance().getItems(AbstractProject.class)) {
                    if (this == project.getScm() ) {
                        CodePlexProjectProperty property = project.getProperty(CodePlexProjectProperty.class);
                        if (property != null) {
                                configuredScm = new TeamFoundationServerScm(
                                    getTfsUrl(property), 
                                    String.format("$/%s%s", property.getProjectName(), path), 
                                    ".", true, null, 
                                    (getUserName() != null ? getTfsUserName(getUserName()) : null), 
                                    (getUserPassword() != null ? getUserPassword() : null));
                        } else {
                            throw new RuntimeException("The project does not have a google code property. Please report this to the plugin author.");
                        }
                        break;
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Could not communicate with the remote CodePlex SOAP server. Please contact plugin author.", e);
            } catch (ServiceException e) {
                throw new RuntimeException("Could not communicate with the remote CodePlex SOAP server. Please contact plugin author.", e);
            }
         if (configuredScm == null) {
            throw new RuntimeException("Could not find the project for this SCM object. Please contact plugin author.");
         }
        }
        return configuredScm;
    }

    private String getTfsUserName(String codePlexUserName) throws RemoteException, ServiceException {
        ProjectInfoServiceLocator locator = new ProjectInfoServiceLocator();
        return locator.getProjectInfoServiceSoap().codePlexUserNameToTfsUserName(codePlexUserName);
    }

    private String getTfsUrl(CodePlexProjectProperty property) throws RemoteException, ServiceException {
        ProjectInfoServiceLocator locator = new ProjectInfoServiceLocator();
        return locator.getProjectInfoServiceSoap().getTfsInfoForProject(property.getProjectName()).getTfsServerUrl();
    }

    @Override
    public RepositoryBrowser getBrowser() {
        if (tfsBrowser == null) {
            tfsBrowser = new CodePlexTfsBrowser();
        }
        return tfsBrowser;
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException,
            InterruptedException {
        return getScm().checkout(build, launcher, workspace, listener, changelogFile);
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new TfsChangeLogParserDecorator(getScm().createChangeLogParser());
    }

    @Override
    public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        return getScm().pollChanges(project, launcher, workspace, listener);
    }
    
    @Override
    public void buildEnvVars(AbstractBuild build, Map<String, String> env) {
        getScm().buildEnvVars(build, env);
    }

    @Override
    public FilePath getModuleRoot(FilePath workspace) {
        return getScm().getModuleRoot(workspace);
    }

    @Override
    public FilePath[] getModuleRoots(FilePath workspace) {
        return getScm().getModuleRoots(workspace);
    }

    @Override
    public boolean processWorkspaceBeforeDeletion(AbstractProject<?, ?> project, FilePath workspace, Node node) throws IOException, InterruptedException {
        return getScm().processWorkspaceBeforeDeletion(project, workspace, node);
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return getScm().requiresWorkspaceForPolling();
    }

    @Override
    public boolean supportsPolling() {
        return getScm().supportsPolling();
    }

    @Override
    public SCMDescriptor<?> getDescriptor() {
        return PluginImpl.TFS_SCM_DESCRIPTOR;
    }
    
    public static class DescriptorImpl extends SCMDescriptor<CodePlexTfsScm> {

        public DescriptorImpl() {
            super(CodePlexTfsScm.class, CodePlexTfsBrowser.class);       
        }
        
        @Override
        public String getDisplayName() {
            return "CodePlex";
        }
    }
}
