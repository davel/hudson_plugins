/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import junit.framework.TestCase;

import com.mockobjects.naming.directory.MockAttribute;
import com.mockobjects.naming.directory.MockAttributes;
import com.mockobjects.naming.directory.MockDirContext;
import com.mockobjects.naming.directory.MockNamingEnumeration;
import com.mtvi.plateng.testing.jndi.MockDirContextFactory;

public abstract class BaseLdapSearchTestCase extends TestCase {
    private MockDirContext mockContext;
    private MockNamingEnumeration mockResults;
    private MockAttributes attrs;
    private MockAttribute attr;

    protected abstract String getLDAPURL();

    private class TestSearchControls extends SearchControls {

        @Override
        public boolean equals(Object obj) {
            SearchControls other = (SearchControls) obj;
            return getSearchScope() == other.getSearchScope();
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockContext = MockDirContextFactory.getContext(getLDAPURL());
        System.out.println("in setup: " + mockContext);
        SearchControls ctrs = new TestSearchControls();
        ctrs.setSearchScope(SearchControls.SUBTREE_SCOPE);

        attrs = new MockAttributes();
        attrs.setExpectedName("email");
        attr = new MockAttribute();
        attr.setupGet("mail@test.com");
        attrs.setupAddGet(attr);

        SearchResult result = new SearchResult("uid=testuser,ou=Users", null, attrs);

        mockResults = new MockNamingEnumeration();
        mockResults.setupAddSearchResult(result);

        mockContext.setExpectedSearch("dc=test,dc=com", "uid=testuser", ctrs);
        mockContext.setupSearchResult(mockResults);
    }

    @Override
    protected void tearDown() throws Exception {
        mockContext.verify();
        attrs.verify();
        attr.verify();
        mockResults.verify();
        MockDirContextFactory.removeContext(getLDAPURL());
        super.tearDown();
    }

}
