/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public class LdapLookupTest extends BaseLdapTestCase {

    public void testLookupUser() throws Exception {
        Configuration config = new Configuration();
        config.setServer(getLDAPURL());
        config.setBaseDN("ou=Users,dc=test,dc=com");
        config.setEmailAttribute("mail");
        config.setSearchAttribute("uid");
        config.setInitialContextFactoryName(MockDirContextFactory.NAME);

        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        assertEquals("mail@test.com", resolver.findMailAddressFor("testuser"));

        mockContext.verify();
    }

    public void testLookupUserWithAuth() throws Exception {
        Configuration config = new Configuration();
        config.setServer(getLDAPURL());
        config.setBaseDN("ou=Users,dc=test,dc=com");
        config.setEmailAttribute("mail");
        config.setSearchAttribute("uid");
        config.setBindDN("bindDN");
        config.setBindPassword("password");
        config.setInitialContextFactoryName(MockDirContextFactory.NAME);

        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        assertEquals("mail@test.com", resolver.findMailAddressFor("testuser"));

        mockContext.verify();
    }

    @Override
    protected String getLDAPURL() {
        return "ldap://test:389";
    }
}
