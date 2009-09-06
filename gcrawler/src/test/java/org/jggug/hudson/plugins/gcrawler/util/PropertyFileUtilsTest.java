package org.jggug.hudson.plugins.gcrawler.util;

import static junit.framework.Assert.*;
import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.PropertyResourceBundle;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class PropertyFileUtilsTest {

    @Test
    public void testToResourceBundleFromText() throws IOException {
        String props = new StringBuilder()
            .append("foo=FOO\n")
            .append("bar=BAR\n").toString();
        PropertyResourceBundle actual = toResourceBundleFromText(props);
        assertNotNull(actual);
        assertEquals("FOO", actual.getString("foo"));
        assertEquals("BAR", actual.getString("bar"));
    }

    @Test
    public void testToResourceBundleFromFile_String() throws IOException {
        File propertyFile = File.createTempFile(getClass().getName(), ".properties");
        String props = new StringBuilder()
            .append("foo=FOO\n")
            .append("bar=BAR\n").toString();
        FileUtils.writeStringToFile(propertyFile, props);
        PropertyResourceBundle actual = toResourceBundleFromFile(propertyFile.getAbsolutePath());
        assertNotNull(actual);
        assertEquals("FOO", actual.getString("foo"));
        assertEquals("BAR", actual.getString("bar"));
    }

    @Test
    public void testToResourceBundleFromFile_File() throws IOException {
        File propertyFile = File.createTempFile(getClass().getName(), ".properties");
        String props = new StringBuilder()
            .append("foo=FOO\n")
            .append("bar=BAR\n").toString();
        FileUtils.writeStringToFile(propertyFile, props);
        PropertyResourceBundle actual = toResourceBundleFromFile(propertyFile);
        assertNotNull(actual);
        assertEquals("FOO", actual.getString("foo"));
        assertEquals("BAR", actual.getString("bar"));
    }

    @Test
    public void testGetStringPropertyValue() throws IOException {
        File propertyFile = File.createTempFile(getClass().getName(), ".properties");
        String props = new StringBuilder()
            .append("foo=FOO\n")
            .append("bar=BAR\n").toString();
        FileUtils.writeStringToFile(propertyFile, props);
        assertEquals("FOO", getStringPropertyValue(propertyFile, "foo"));
        assertEquals("BAR", getStringPropertyValue(propertyFile, "bar"));
    }
}
