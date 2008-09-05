/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import junit.framework.TestCase;

import com.mockobjects.naming.directory.MockAttribute;
import com.mockobjects.naming.directory.MockAttributes;
import com.mockobjects.naming.directory.MockDirContext;
import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public abstract class BaseLdapTestCase extends TestCase {
    protected MockDirContext mockContext;

    protected abstract String getLDAPURL();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockContext = MockDirContextFactory.getContext(getLDAPURL());
        System.out.println("in setup: " + mockContext);
        mockContext.setExpectedGetAttributesName("uid=testuser,ou=Users,dc=test,dc=com");
        MockAttributes attrs = new MockAttributes();
        attrs.setExpectedName("mail");
        MockAttribute attr = new MockAttribute();
        attr.setupGet("mail@test.com");
        attrs.setupAddGet(attr);
        mockContext.setupAttributes(attrs);
    }

    @Override
    protected void tearDown() throws Exception {
        MockDirContextFactory.removeContext(getLDAPURL());
        super.tearDown();
    }

}
