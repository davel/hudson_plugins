/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.mockobjects.servlet.MockServletContext;

public class HudsonUtil {

    private static Hudson hudson;
    private static File root;
    private static MockServletContext servletContext;

    public static void initHudson() {
        root = new File("target/test-hudson");
        root.mkdirs();
        servletContext = new MockServletContext();
        Hudson temp = null;
        try {
            temp = new Hudson(root, servletContext);
        } catch (IOException e) {
            System.err.println("Exception creating Hudson instance.");
            e.printStackTrace();
        }
        hudson = temp;
    }

    public static void cleanUpHudson() throws IOException {
        HudsonUtil.hudson.cleanUp();
        FileUtils.deleteDirectory(HudsonUtil.root);
    }

    public static File getRootDirectory() {
        return root;
    }

}
