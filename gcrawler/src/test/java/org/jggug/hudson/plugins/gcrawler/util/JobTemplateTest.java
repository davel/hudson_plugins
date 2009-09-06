package org.jggug.hudson.plugins.gcrawler.util;

import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;
import org.junit.Test;

public class JobTemplateTest {

    @Test
    public void generate() {
        JobTemplate template = new JobTemplate("#{foo}#{bar}#{foo}");
        Source source = new Source();
        source.setFoo("FOO");
        source.setBar("BAR");
        assertEquals("FOOBARFOO", template.generate(source));
    }

    @Test
    public void generateNullProperties() {
        JobTemplate template = new JobTemplate("#{foo}#{bar}#{foo}");
        assertEquals("", template.generate(new Source()));
    }

    @Test(expected=RuntimeException.class)
    public void generateNoProperties() {
        JobTemplate template = new JobTemplate("#{foo}#{bar}#{foo}#{hoge}");
        template.generate(new Source());
    }

    @Test
    public void generateListProperty() {
        JobTemplate template = new JobTemplate("#{list}");
        ListBean source = new ListBean();
        source.setList(Arrays.asList("foo", "bar", "baz"));
        assertEquals("foo, bar, baz", template.generate(source));
    }

    @Test
    public void createTemplate() {
        assertNotNull(JobTemplate.createTemplate("google_grails_description.txt"));
        assertNotNull(JobTemplate.createTemplate("google_grails_shell.txt"));
    }

    @Test(expected=RuntimeException.class)
    public void createTemplateNotFound() {
        assertNotNull(JobTemplate.createTemplate("not_found"));
    }

    public static class Source {
        private String foo;
        private String bar;
        public String getFoo() {
            return foo;
        }
        public String getBar() {
            return bar;
        }
        public void setFoo(String foo) {
            this.foo = foo;
        }
        public void setBar(String bar) {
            this.bar = bar;
        }
    }

    public static class ListBean {
        private List<String> list;
        public List<String> getList() {
            return list;
        }
        public void setList(List<String> list) {
            this.list = list;
        }
    }
}
