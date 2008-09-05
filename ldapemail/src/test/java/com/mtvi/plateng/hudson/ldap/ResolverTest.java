/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import hudson.model.Hudson;
import hudson.model.User;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.mockobjects.servlet.MockServletContext;
import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public class ResolverTest extends BaseLdapTestCase {

    @Override
    protected String getLDAPURL() {
        return "ldap://" + ResolverTest.class.getName();
    }

    protected Hudson hudson;
    protected File root;
    protected MockServletContext servletContext;

    public void testLookupUser() throws Exception {
        Configuration config = new Configuration();
        config.setServer(getLDAPURL());
        config.setBaseDN("ou=Users,dc=test,dc=com");
        config.setEmailAttribute("mail");
        config.setSearchAttribute("uid");
        config.setInitialContextFactoryName(MockDirContextFactory.NAME);

        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        User u = User.get("testuser");
        assertEquals("mail@test.com", resolver.findMailAddressFor(u));

        mockContext.verify();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        hudson = HudsonUtil.hudson;
    }

    @Override
    protected void tearDown() throws Exception {
        HudsonUtil.hudson.cleanUp();
        FileUtils.deleteDirectory(HudsonUtil.root);
        super.tearDown();
    }
}
