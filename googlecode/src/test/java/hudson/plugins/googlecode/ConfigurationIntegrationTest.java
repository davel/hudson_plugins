package hudson.plugins.googlecode;

import hudson.model.FreeStyleProject;
import hudson.plugins.googlecode.scm.GoogleCodeSCMEx;
import hudson.scm.SubversionSCM.ModuleLocation;

import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class ConfigurationIntegrationTest extends HudsonTestCase {
    /**
     * Asserts that configuration works
     * @throws Exception thrown if problem
     */
    public void testOldConfiguredRepositoryBrowserCanBeCreated() throws Exception {

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GoogleCodeSCMEx("path2", new ModuleLocation("http://leetdev3da.googlecode.com/svn/tags/tag", ".")));
        project.addProperty(new GoogleCodeProjectProperty("http://www.googlecode.com/p/mockitopp"));

        HtmlForm form = new WebClient().getPage(project,"configure").getFormByName("config");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertEquals("The project's SCM wasnt Google code", GoogleCodeSCMEx.class,  project.getScm().getClass());
    }
}