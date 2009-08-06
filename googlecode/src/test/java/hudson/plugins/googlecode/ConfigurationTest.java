package hudson.plugins.googlecode;

import hudson.model.FreeStyleProject;
import hudson.plugins.googlecode.scm.GoogleCodeSCM;

import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class ConfigurationTest extends HudsonTestCase {
    /**
     * Asserts that configuration works
     * @throws Exception thrown if problem
     */
    public void testConfiguredRepositoryBrowserCanBeCreated() throws Exception {

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GoogleCodeSCM("path2"));

        HtmlForm form = new WebClient().getPage(project,"configure").getFormByName("config");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertEquals("The project's SCM wasnt Google code", GoogleCodeSCM.class,  project.getScm().getClass());
    }
}
