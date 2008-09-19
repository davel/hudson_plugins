package hudson.plugins.codeplex.scm;

import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import com.codeplex.soap.ProjectInfoService;
import com.codeplex.soap.ProjectInfoServiceLocator;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.View;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.tfs.TeamFoundationServerScm;

public class TeamFoundationServerScmFactory {

    private final ProjectInfoService service;
    private final CodePlexTfsScm scm;

    public TeamFoundationServerScmFactory(ProjectInfoService service, CodePlexTfsScm scm) {
        this.service = service;
        this.scm = scm;
    }

    public TeamFoundationServerScm create(List<AbstractProject<?,?>> projects) {
        TeamFoundationServerScm configuredScm = null;
        try {
            for (AbstractProject<?, ?> project : projects) {
                if (scm == project.getScm() ) {
                    CodePlexProjectProperty property = project.getProperty(CodePlexProjectProperty.class);
                    if (property != null) {
                            configuredScm = new TeamFoundationServerScm(
                                getTfsUrl(property), 
                                String.format("$/%s%s", property.getProjectName(), scm.getPath()), 
                                ".", true, null, 
                                getTfsUsername(scm.getUserName()), 
                                (scm.getUserPassword() != null ? scm.getUserPassword() : null));
                    } else {
                        throw new RuntimeException("This project does not have a CodePlex property configure. Please configure the name of the Code Plex project.");
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
        return configuredScm;
    }

    private String getTfsUsername(String codeplexUsername) throws RemoteException,ServiceException {
        if (codeplexUsername == null) { 
            return null; 
        } else {
            return service.getProjectInfoServiceSoap().codePlexUserNameToTfsUserName(codeplexUsername);
        }
    }

    private String getTfsUrl(CodePlexProjectProperty property) throws RemoteException, ServiceException {
        return service.getProjectInfoServiceSoap().getTfsInfoForProject(property.getProjectName()).getTfsServerUrl();
    }
}
