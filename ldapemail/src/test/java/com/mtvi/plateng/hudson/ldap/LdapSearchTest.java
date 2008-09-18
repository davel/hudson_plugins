/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public class LdapSearchTest extends BaseLdapSearchTestCase {

    public void testSearchUser() throws Exception {
        Configuration config = new Configuration();
        config.setServer(getLDAPURL());
        config.setBaseDN("dc=test,dc=com");
        config.setEmailAttribute("email");
        config.setSearchAttribute("uid");
        config.setPerformSearch(true);
        config.setInitialContextFactoryName(MockDirContextFactory.NAME);

        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        assertEquals("mail@test.com", resolver.findMailAddressFor("testuser"));
    }

    public void testSearchUserWithAuth() throws Exception {
        Configuration config = new Configuration();
        config.setServer(getLDAPURL());
        config.setBaseDN("dc=test,dc=com");
        config.setEmailAttribute("email");
        config.setSearchAttribute("uid");
        config.setPerformSearch(true);
        config.setBindDN("bindDN");
        config.setBindPassword("password");
        config.setInitialContextFactoryName(MockDirContextFactory.NAME);

        LdapMailAddressResolver resolver = new LdapMailAddressResolver(config);
        assertEquals("mail@test.com", resolver.findMailAddressFor("testuser"));
    }

    @Override
    protected String getLDAPURL() {
        return "ldap://test:389";
    }
}
