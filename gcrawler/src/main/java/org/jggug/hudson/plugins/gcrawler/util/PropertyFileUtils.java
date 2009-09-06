package org.jggug.hudson.plugins.gcrawler.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.filters.StringInputStream;

public class PropertyFileUtils {

    public static PropertyResourceBundle toResourceBundleFromFile(String file) throws IOException {
        return toResourceBundleFromFile(new File(file));
    }

    public static PropertyResourceBundle toResourceBundleFromFile(File file) throws IOException {
        return toResouceBundle(new FileInputStream(file));
    }

    public static PropertyResourceBundle toResourceBundleFromText(String text) throws IOException {
        return toResouceBundle(new StringInputStream(text));
    }

    private static PropertyResourceBundle toResouceBundle(InputStream in) throws IOException {
        try {
            return new PropertyResourceBundle(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String getStringPropertyValue(PropertyResourceBundle b, String key) {
        try {
            return b.getString(key);
        } catch (MissingResourceException ignore) {
            return null;
        }
    }

    public static String getStringPropertyValue(File file, String key) {
        try {
            return getStringPropertyValue(toResourceBundleFromFile(file), key);
        } catch (IOException e) {
            return null;
        }
    }
}
