package com.thalesgroup.hudson.plugins.ucm4svn.model;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class XSDTest {

    private Response parseXML(String fileName) throws URISyntaxException, JAXBException {
        File f = new File(this.getClass().getResource(fileName).toURI());
        JAXBContext jc = JAXBContext.newInstance("com.thalesgroup.hudson.plugins.ucm4svn.model");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Response response = (Response) unmarshaller.unmarshal(f);
        return response;
    }

    @Test
    public void connexion() throws Exception {
        Response response = parseXML("getConnexion.xml");
        assert response.getToken() != null;

        Status status = response.getStatus();
        assert status != null;
        assert status.getSuccess() == "true";
        assert status.getValue() == "ok";
    }


    @Test
    public void projects() throws Exception {
        Response response = parseXML("getProjects.xml");

        Projects projects = response.getProjects();
        assert projects != null;

        List<Project> listeProjects = projects.getProject();
        assert listeProjects != null;
        assert listeProjects.size() == 8;

        //First project in the list
        Project p1 = listeProjects.get(0);
        assert p1 != null;
        assert p1.getId() == "8";
        assert p1.getName() == "without_jira";

        //Middle project in the list
        Project p2 = listeProjects.get(4);
        assert p2 != null;
        assert p2.getId() == "4";
        assert p2.getName() == "demo project";


        //Last project in the list
        Project p3 = listeProjects.get(listeProjects.size() - 1);
        assert p3 != null;
        assert p3.getId() == "1";
        assert p3.getName() == "project1";


    }

    @Test
    public void createBaseline() throws Exception {
        Response response = parseXML("createBaseline.xml");

        Status status = response.getStatus();
        assert status != null;
        assert status.getSuccess() == "true";
        assert status.getValue() == "Baseline created";
    }


    @Test
    public void activitiesFlow() throws Exception {
        Response response = parseXML("getActivities.xml");

        Activities activities = response.getActivities();
        assert activities != null;
        assert activities.getActivity().size() == 2;

        //Test the first activity
        Activity activity1 = activities.getActivity().get(0);
        assert activity1 != null;
        assert activity1.getId() == "7";
        assert activity1.getName() == "bidon";

        //Test the second activity
        Activity activity2 = activities.getActivity().get(0);
        assert activity2 != null;
        assert activity2.getId() == "8";
        assert activity2.getName() == "bidon2";
    }


    @Test
    public void changesetFlow1() throws Exception {

        Response response = parseXML("getChangeset-1.xml");

        Changeset changeset = (Changeset) response.getChangeset();
        assert changeset != null;

        List<Component> components = changeset.getComponent();
        assert components.size() == 1;

        //Test first component
        Component c = components.get(0);
        assert c != null;
        assert c.getId() == "1";
        assert c.getName() == "test1";

        //TODO : Test the svm changet set part
    }

    @Test
    public void changesetFlow2() throws Exception {
        Response response = parseXML("getChangeset-2.xml");

        // TODO TO check
    }


    @Test
    public void checkoutCommand() throws Exception {
        Response response = parseXML("getCheckoutCommands.xml");

        CheckoutCommands checkoutCommands = response.getCheckoutCommands();
        assert checkoutCommands != null;

        List<String> commands = checkoutCommands.getCheckoutCommand();

        assert commands != null;
        assert commands.size() == 1;

        //Fist commnad
        String command = commands.get(0);
        assert command == "svn co \"http://vmo56/ucm4svn/test1_1/branches/projects/project1_1&quot; &quot;project1_1/test1\"";


    }

}
