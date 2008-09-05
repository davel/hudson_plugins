/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import hudson.model.Hudson;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mtvi.plateng.testing.hudson.HudsonUtil;

public class BadConfigLoadTest {

    @Test
    public void testBadConfig() throws IOException {
        PluginImpl pi = new PluginImpl();
        Configuration config = pi.loadConfiguration();
        Assert.assertFalse(config.isValid());
    }

    @Test
    public void testLdapMailAddressResolver() throws Exception {
        PluginImpl pi = new PluginImpl();
        Configuration config = pi.loadConfiguration();
        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        Assert.assertNull(resolver.findMailAddressFor("username"));
    }

    @Before
    public void setUp() throws Exception {
        HudsonUtil.initHudson();
        Assert.assertNotNull(Hudson.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        HudsonUtil.cleanUpHudson();
    }
}
