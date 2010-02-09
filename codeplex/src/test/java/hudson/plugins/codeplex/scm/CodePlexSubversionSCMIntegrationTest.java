package hudson.plugins.codeplex.scm;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.codeplex.CodePlexProjectProperty;
import hudson.plugins.codeplex.scm.CodePlexSubversionSCM;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class CodePlexSubversionSCMIntegrationTest extends HudsonTestCase {

    /**
     * Asserts that Code plex subversion SCM works with Matrix projects.
     * @throws Exception thrown if problem
     */
    public void testCodePlexSCMExNoLongerThrowsExceptionInAMatrixProject() throws Exception {
        MatrixProject project = createMatrixProject("matrix");
        project.setScm(new CodePlexSubversionSCM("path2", new ModuleLocation("https://rawr.svn.codeplex.com/svn", ".")));
        project.addProperty(new CodePlexProjectProperty("rawr"));
        
        // Call any SCM method on the code plex scm used to throw RuntimeException
        // when not finding the correct project
        project.getScm().getBrowser();
    }
  
    public void testCodePlexSCMExExtendsSubversionScm() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        WebClient client = new WebClient();
        HtmlForm form = client.getPage(project, "configure").getFormByName("config");
        form.getInputByName("codeplex.projectName").setValueAttribute("rawr");
        getCodeplexHtmlInput(form).click(); // 
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
        form.getInputByName("codeplex.projectName").setValueAttribute("rawr");
        form.getInputByName("codeplex.svnRemoteDirectory").setValueAttribute("tags/tag");
        getCodeplexHtmlInput(form).click(); // 
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        assertThat(((CodePlexSubversionSCM) project.getScm()).getLocations()[0].getURL(), is("https://rawr.svn.codeplex.com/svn/tags/tag")); 
    }
  
    /**
     * Asserts that the svn URL is appended with the directory
     * @throws Exception thrown if test errors
     */
    public void testLocalDirectoryIsStoredCorrectly() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        WebClient client = new WebClient();
        HtmlForm form = client.getPage(project, "configure").getFormByName("config");
        form.getInputByName("codeplex.projectName").setValueAttribute("rawr");
        form.getInputByName("codeplex.svnRemoteDirectory").setValueAttribute("tags/tag");
        getCodeplexHtmlInput(form).click(); // 
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        
        assertEquals("SVN Directory is incorrect", "tags/tag", ((CodePlexSubversionSCM) project.getScm()).getDirectory());
        
        HtmlPage page = client.getPage(project, "configure");
        WebAssert.assertInputContainsValue(page, "codeplex.svnRemoteDirectory", "tags/tag"); 
    }

    public void testNewInstance() {
        CodePlexSubversionSCM scm = CodePlexSubversionSCM.DescriptorImpl.newInstance(new CodePlexProjectProperty("rawr"), "trunk");
        assertThat(scm.getLocations().length, is(1));
        ModuleLocation location = scm.getLocations()[0];
        assertThat(location.getLocalDir(), is("."));
        assertThat(location.getURL(), is("https://rawr.svn.codeplex.com/svn/trunk"));
    }
    
    public void testConfiguringWithoutSettingCodePlexProjectNameThrowsException() throws Exception {
        try {
            FreeStyleProject project = createFreeStyleProject();
            
            WebClient client = new WebClient();
            HtmlForm form = client.getPage(project, "configure").getFormByName("config");
            form.getInputByName("codeplex.svnRemoteDirectory").setValueAttribute("tags/tag");
            getCodeplexHtmlInput(form).click(); // 
            form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        } catch (FailingHttpStatusCodeException e) {            
        }
    }


    private HtmlInput getCodeplexHtmlInput(HtmlForm form) {
    	for (HtmlInput input: form.getInputsByName("scm")) {
    		for (DomNode node : input.getParentNode().getChildren()) {
    			if (node.getNodeName().equals("label")) {
    				if (node.getTextContent().equals(CodePlexSubversionSCM.DescriptorImpl.DISPLAY_NAME)) {
    					return input;
    				}
    			}
    		}
    	}
    	fail("No HTML input tag found for " + CodePlexSubversionSCM.DescriptorImpl.DISPLAY_NAME);
    	throw new RuntimeException();
    }
}
