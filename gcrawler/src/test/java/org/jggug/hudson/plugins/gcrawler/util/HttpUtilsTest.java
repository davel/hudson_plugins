package org.jggug.hudson.plugins.gcrawler.util;

import static junit.framework.Assert.*;

import org.junit.Test;

public class HttpUtilsTest {

    @Test
    public void joinAsPath() {
        assertEquals("foo/bar/baz", HttpUtils.joinAsPath("foo", "bar", "baz"));
    }

    @Test
    public void joinAsPath_Path_File() {
        assertEquals("/foo/bar/baz", HttpUtils.joinAsPath("/foo/bar", "baz"));
    }

    @Test
    public void joinAsPath_One() {
        assertEquals("foo", HttpUtils.joinAsPath("foo"));
    }

    @Test
    public void joinAsPath_Null() {
        assertEquals("", HttpUtils.joinAsPath((String) null));
    }

    @Test
    public void joinAsPath_Empty() {
        assertEquals("", HttpUtils.joinAsPath((String) ""));
    }
}
