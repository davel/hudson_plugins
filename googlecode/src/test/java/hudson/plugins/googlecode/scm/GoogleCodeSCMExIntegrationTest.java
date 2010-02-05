package hudson.plugins.googlecode.scm;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class GoogleCodeSCMExIntegrationTest extends HudsonTestCase {

    /**
     * Asserts that google code SCM works with Matrix projects.
     * @throws Exception thrown if problem
     */
    public void testGoogleCodeSCMExNoLongerThrowsExceptionInAMatrixProject() throws Exception {
        MatrixProject project = createMatrixProject("matrix");
        project.setScm(new GoogleCodeSCMEx("path2", new ModuleLocation("http://leetdev3da.googlecode.com/svn/trunk", ".")));
        project.addProperty(new GoogleCodeProjectProperty("http://www.googlecode.com/p/mockitopp"));
        
        // Call any SCM method on the google code used to throw RuntimeException
        // when not finding the correct project
        project.getScm().getBrowser();
    }
  
    /**
     * Asserts that the Google code SCM extends the SubversionSCM class so we do not get any more class casting exceptions.
     * @throws Exception thrown if test errors
     */
    @Bug(4136)
    public void testGoogleCodeSCMExExtendsSubversionScm() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        WebClient client = new WebClient();
        HtmlForm form = client.getPage(project, "configure").getFormByName("config");
        form.getInputByName("googlecode.googlecodeWebsite").setValueAttribute("http://code.google.com/p/leetdev3da/");
        form.getInputsByName("scm").get(0).click(); // 
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        assertThat(project.getScm(), notNullValue());
        assertThat(project.getScm(), is(SubversionSCM.class)); 
    }
  
    /**
     * Asserts that the svn URL is appended with the directory
     * @throws Exception thrown if test errors
     */
    public void testSvnDirectoryIsSetCorrectly() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        WebClient client = new WebClient();
        HtmlForm form = client.getPage(project, "configure").getFormByName("config");
        form.getInputByName("googlecode.googlecodeWebsite").setValueAttribute("http://code.google.com/p/leetdev3da/");
        form.getInputByName("googlecode.svnRemoteDirectory").setValueAttribute("tags/tag");
        form.getInputsByName("scm").get(0).click(); // 
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        assertThat(((GoogleCodeSCMEx) project.getScm()).getLocations()[0].getURL(), is("http://leetdev3da.googlecode.com/svn/tags/tag")); 
    }
  
    /**
     * Asserts that the svn URL is appended with the directory
     * @throws Exception thrown if test errors
     */
    public void testLocalDirectoryIsStoredCorrectly() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        WebClient client = new WebClient();
        HtmlForm form = client.getPage(project, "configure").getFormByName("config");
        form.getInputByName("googlecode.googlecodeWebsite").setValueAttribute("http://code.google.com/p/leetdev3da/");
        form.getInputByName("googlecode.svnRemoteDirectory").setValueAttribute("tags/tag");
        form.getInputsByName("scm").get(0).click(); // 
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        assertEquals("SVN Directory is incorrect", "tags/tag", ((GoogleCodeSCMEx) project.getScm()).getDirectory());
        
        HtmlPage page = client.getPage(project, "configure");
        WebAssert.assertInputContainsValue(page, "googlecode.svnRemoteDirectory", "tags/tag"); 
    }

    public void testNewInstance() {
        GoogleCodeSCMEx scm = GoogleCodeSCMEx.DescriptorImpl.newInstance(new GoogleCodeProjectProperty("http://code.google.com/p/leetdev3da/"), "trunk");
        assertThat(scm.getLocations().length, is(1));
        ModuleLocation location = scm.getLocations()[0];
        assertThat(location.getLocalDir(), is("."));
        assertThat(location.getURL(), is("http://leetdev3da.googlecode.com/svn/trunk"));
    }
    
    public void testConfiguringWithoutSettingGoogleCodeProjectUrlThrowsException() throws Exception {
        try {
            FreeStyleProject project = createFreeStyleProject();
            
            WebClient client = new WebClient();
            HtmlForm form = client.getPage(project, "configure").getFormByName("config");
            form.getInputByName("googlecode.svnRemoteDirectory").setValueAttribute("tags/tag");
            form.getInputsByName("scm").get(0).click(); // 
            form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        } catch (FailingHttpStatusCodeException e) {            
        }
    }

    public void testGetDescriptorReturnsCorrectDescriptor() throws Exception {
        GoogleCodeSCMEx scm = new GoogleCodeSCMEx("path2", new ModuleLocation("http://leetdev3da.googlecode.com/svn/trunk", "."));
        assertThat(scm.getDescriptor(), is(GoogleCodeSCMEx.DescriptorImpl.class));
    }
}
