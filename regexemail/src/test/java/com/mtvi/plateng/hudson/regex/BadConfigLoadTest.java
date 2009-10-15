/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.regex;

import hudson.model.Hudson;
import hudson.model.User;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class BadConfigLoadTest extends HudsonTestCase {

    @Test
    public void testBadConfig() throws IOException {
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

    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        FileUtils.copyFile(new File("src/test/resources/unit/bad-config.xml"), new File(
                Hudson.getInstance().getRootDir(), RegexMailAddressResolver.class.getName() + ".xml"));
    }
}
