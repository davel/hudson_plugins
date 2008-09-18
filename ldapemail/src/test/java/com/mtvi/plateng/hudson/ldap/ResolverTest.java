/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import hudson.model.User;

import com.mtvi.plateng.testing.hudson.HudsonUtil;
import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public class ResolverTest extends BaseLdapDNLookupTestCase {

    @Override
    protected String getLDAPURL() {
        return "ldap://" + ResolverTest.class.getName();
    }

    public void testLookupUser() throws Exception {
        Configuration config = new Configuration();
        config.setServer(getLDAPURL());
        config.setBaseDN("ou=Users,dc=test,dc=com");
        config.setEmailAttribute("email");
        config.setSearchAttribute("uid");
        config.setInitialContextFactoryName(MockDirContextFactory.NAME);

        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        User u = User.get("testuser");
        assertEquals("mail@test.com", resolver.findMailAddressFor(u));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HudsonUtil.initHudson();
    }

    @Override
    protected void tearDown() throws Exception {
        HudsonUtil.cleanUpHudson();
        super.tearDown();
    }
}
