package org.jggug.hudson.plugins.gcrawler.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jggug.hudson.plugins.gcrawler.SVNFIleInfo;

public class HttpUtils {

    public static SVNFIleInfo getApplicationProperties(String svnUrl) throws FileNotFoundException {
        return getFile(joinAsPath(svnUrl, "application.properties"));
    }

    public static SVNFIleInfo getFile(String url) throws FileNotFoundException {
        InputStream in = null;
        try {
            URL u = new URL(url.replaceAll(" ", "%20"));
            in = u.openStream();
            return new SVNFIleInfo(url, IOUtils.toString(in));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static boolean existsFile(String url) {
        InputStream in = null;
        try {
            URL u = new URL(url.replaceAll(" ", "%20"));
            in = u.openStream();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String joinAsPath(String... args) {
        return StringUtils.join(args, "/");
    }

}
