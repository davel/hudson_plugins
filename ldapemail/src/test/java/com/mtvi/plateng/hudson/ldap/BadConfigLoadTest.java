/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import java.io.IOException;

import org.junit.Assert;
import org.jvnet.hudson.test.HudsonTestCase;

public class BadConfigLoadTest extends HudsonTestCase {

    public void testBadConfig() throws IOException {
        PluginImpl pi = new PluginImpl();
        Configuration config = pi.loadConfiguration();
        Assert.assertFalse(config.isValid());
    }

    public void testLdapMailAddressResolver() throws Exception {
        PluginImpl pi = new PluginImpl();
        Configuration config = pi.loadConfiguration();
        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        Assert.assertNull(resolver.findMailAddressFor("username"));
    }
}
