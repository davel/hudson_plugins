package hudson.plugins.codeplex.scm;

import static org.junit.Assert.*;

import hudson.model.View;
import hudson.plugins.tfs.TeamFoundationServerScm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;

import com.codeplex.soap.ProjectInfoService;
import com.codeplex.soap.ProjectInfoServiceLocator;

public class TeamFoundationServerScmFactoryTest {

    @Mock private View view;
    @Mock private ProjectInfoService service;
    
    @Before public void setUp() {
        MockitoAnnotations.initMocks(TeamFoundationServerScmFactoryTest.class);
    }
    
    @Test public void assertCreate() {
//        TeamFoundationServerScmFactory factory = new TeamFoundationServerScmFactory(view, service);
//        TeamFoundationServerScm scm = factory.create("ProjectName", "UserName", "Password", "path");
//        assertNotNull("Create scm was not null", scm);
    }
}
