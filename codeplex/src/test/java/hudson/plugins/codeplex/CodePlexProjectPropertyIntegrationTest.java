package hudson.plugins.codeplex;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;

import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class CodePlexProjectPropertyIntegrationTest extends HudsonTestCase {

    public void testNoPropertyIsAddedToProjectIfCodeplexNameIsEmpty() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        HtmlForm form = new WebClient().getPage(project, "configure").getFormByName("config");
        form.getInputByName("codeplex.projectName").setValueAttribute("");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(project.getProperty(CodePlexProjectProperty.class), nullValue()); 
    }
    
    public void testPropertyIsAddedToProject() throws Exception {        
        FreeStyleProject project = createFreeStyleProject();
        
        HtmlForm form = new WebClient().getPage(project, "configure").getFormByName("config");
        form.getInputByName("codeplex.projectName").setValueAttribute("rawr");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        CodePlexProjectProperty property = project.getProperty(CodePlexProjectProperty.class);
        assertThat(property, notNullValue());
        assertThat(property.getProjectName(), is("rawr"));
    }
}
