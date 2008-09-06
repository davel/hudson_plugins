package hudson.plugins.codeplex;

import static org.junit.Assert.*;
import hudson.plugins.codeplex.CodePlexProjectProperty;

import org.junit.Test;

public class CodePlexProjectPropertyTest {

    @Test
    public void testGetProjectName() {
        CodePlexProjectProperty property = new CodePlexProjectProperty("project");
        assertEquals("project", property.getProjectName());
    }

    @Test
    public void assertProjectUrlString() {
        CodePlexProjectProperty property = new CodePlexProjectProperty("project");
        assertEquals("http://www.codeplex.com/project/", property.getProjectUrlString());
    }
}
