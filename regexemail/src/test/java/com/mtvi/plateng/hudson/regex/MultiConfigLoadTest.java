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
public class MultiConfigLoadTest extends HudsonTestCase {

    @Test
    public void testGoodConfig() throws IOException {
        IConfiguration config = PluginImpl.loadConfiguration();
        Assert.assertEquals(MultiConfiguration.class, config.getClass());
        Assert.assertTrue(config.isValid());
    }

    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        FileUtils.copyFile(new File("src/test/resources/unit/multi-config.xml"), new File(
                Hudson.getInstance().getRootDir(), RegexMailAddressResolver.class.getName() + ".xml"));
    }
}
