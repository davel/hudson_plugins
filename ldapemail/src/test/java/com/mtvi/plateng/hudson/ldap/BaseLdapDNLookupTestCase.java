/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import org.jvnet.hudson.test.HudsonTestCase;

import com.mockobjects.naming.directory.MockAttribute;
import com.mockobjects.naming.directory.MockAttributes;
import com.mockobjects.naming.directory.MockDirContext;
import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public abstract class BaseLdapDNLookupTestCase extends HudsonTestCase {
    private MockDirContext mockContext;
    private MockAttributes attrs;
    private MockAttribute attr;

    protected abstract String getLDAPURL();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockContext = MockDirContextFactory.getContext(getLDAPURL());
        System.out.println("in setup: " + mockContext);
        mockContext.setExpectedGetAttributesName("uid=testuser,ou=Users,dc=test,dc=com");
        attrs = new MockAttributes();
        attrs.setExpectedName("email");
        attr = new MockAttribute();
        attr.setupGet("mail@test.com");
        attrs.setupAddGet(attr);
        mockContext.setupAttributes(attrs);
    }

    @Override
    protected void tearDown() throws Exception {
        mockContext.verify();
        attrs.verify();
        attr.verify();
        MockDirContextFactory.removeContext(getLDAPURL());
        super.tearDown();
    }

}
