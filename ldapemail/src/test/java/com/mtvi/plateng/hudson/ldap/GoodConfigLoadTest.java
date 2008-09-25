/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class GoodConfigLoadTest extends HudsonTestCase {

    @Test
    public void testGoodConfig() throws IOException {
        PluginImpl pi = new PluginImpl();
        Configuration config = pi.loadConfiguration();
        Assert.assertTrue(config.isValid());

    }

    @Before
    public void setUp() throws Exception {
        withExistingHome(new File("src/test/resources/unit/goodconfig"));
        super.setUp();
    }
}
