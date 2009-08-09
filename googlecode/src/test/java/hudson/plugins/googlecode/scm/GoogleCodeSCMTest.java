package hudson.plugins.googlecode.scm;

import java.util.Arrays;

import hudson.matrix.MatrixProject;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@SuppressWarnings("unchecked")
public class GoogleCodeSCMTest extends HudsonTestCase {

    /**
     * Asserts that google code SCM works with Matrix projects.
     * @throws Exception thrown if problem
     */
    public void testGoogleCodeScmNoLongerThrowsExceptionInAMatrixProject() throws Exception {
        MatrixProject project = createMatrixProject("matrix");
        project.setScm(new GoogleCodeSCM("path2", Arrays.asList(new ModuleLocation("http://leetdev3da.googlecode.com/svn/trunk", "."))));
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
    public void testGoogleCodeScmExtendsSubversionScm() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        WebClient client = new WebClient();
        HtmlForm form = client.getPage(project, "configure").getFormByName("config");
        form.getInputByName("googlecode.googlecodeWebsite").setValueAttribute("http://code.google.com/p/leetdev3da/");
        form.getInputsByName("scm").get(0).click(); // 
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        assertNotNull("SCM can not be null", project.getScm());
        assertTrue("Google code SCM is not a subversion instance", project.getScm() instanceof SubversionSCM); 
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
        
        assertEquals("SVN URL is incorrect", "http://leetdev3da.googlecode.com/svn/tags/tag", ((GoogleCodeSCM) project.getScm()).getLocations()[0].getURL()); 
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
        
        assertEquals("SVN Directory is incorrect", "tags/tag", ((GoogleCodeSCM) project.getScm()).getDirectory());
        
        HtmlPage page = client.getPage(project, "configure");
        WebAssert.assertInputContainsValue(page, "googlecode.svnRemoteDirectory", "tags/tag"); 
}
    
    public void testModuleLocationIsCorrectlyRetrievedFromGoogleCodeWebsite() {
        ModuleLocation location = GoogleCodeSCM.DescriptorImpl.getModuleLocations("http://code.google.com/p/leetdev3da/", "trunk", ".").get(0);
        assertEquals("Local path is incorrect", ".", location.getLocalDir());
        assertEquals("URL is incorrect", "http://leetdev3da.googlecode.com/svn/trunk", location.getURL());
    }
    
    public void testGetDescriptorReturnsCorrectDescriptor() throws Exception {
        GoogleCodeSCM scm = new GoogleCodeSCM("path2", Arrays.asList(new ModuleLocation("http://leetdev3da.googlecode.com/svn/trunk", ".")));
        assertTrue("getDescriptor() did not return an instance of GoogleCodeSCM.DescriptorImpl",((Descriptor) scm.getDescriptor()) instanceof GoogleCodeSCM.DescriptorImpl);
    }
}
