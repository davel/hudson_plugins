/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mtvi.plateng.testing.hudson.HudsonUtil;

public class GoodConfigLoadTest {

    @Test
    public void testGoodConfig() throws IOException {
        PluginImpl pi = new PluginImpl();
        Configuration config = pi.loadConfiguration();
        Assert.assertTrue(config.isValid());

    }

    @Before
    public void setUp() throws Exception {
        HudsonUtil.initHudson();
        FileUtils.copyFile(new File("src/test/resources/unit/config.xml"), new File(HudsonUtil
                .getRootDirectory(), LdapMailAddressResolver.class.getName() + ".xml"));
    }

    @After
    public void tearDown() throws Exception {
        HudsonUtil.cleanUpHudson();
    }
}
