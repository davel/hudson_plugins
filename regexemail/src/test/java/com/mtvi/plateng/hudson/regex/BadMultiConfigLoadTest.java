/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.regex;

import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author edelsonj
 * 
 */
public class BadMultiConfigLoadTest extends HudsonTestCase {

    @Test
    public void testBadConfig() throws IOException {
        IConfiguration config = PluginImpl.loadConfiguration();
        Assert.assertEquals(MultiConfiguration.class, config.getClass());
        Assert.assertFalse(config.isValid());
    }

    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        FileUtils.copyFile(new File("src/test/resources/unit/bad-multi-config.xml"), new File(
                Hudson.getInstance().getRootDir(), RegexMailAddressResolver.class.getName() + ".xml"));
    }
}
