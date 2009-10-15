/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.regex;

import hudson.model.User;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class MissingConfigLoadTest extends HudsonTestCase {

    @Test
    public void testMissingConfig() throws IOException {
        Configuration config = (Configuration) PluginImpl.loadConfiguration();
        Assert.assertFalse(config.isValid());
    }

    @Test
    public void testRegexMailAddressResolver() throws Exception {
        Configuration config = (Configuration) PluginImpl.loadConfiguration();
        RegexMailAddressResolver resolver = new RegexMailAddressResolver(config);
        User u = User.get("username");
        Assert.assertNull(resolver.findMailAddressFor(u));
    }
}
