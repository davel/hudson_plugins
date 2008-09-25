/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Class to store configuration for the plugin.
 * 
 * @author justinedelson
 * 
 */
public class Configuration {

    /**
     * Default value for the initialContextFactoryName property. Assumes a Sun
     * JDK.
     */
    public static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    /**
     * The base String for building distinguised name references.
     */
    private String baseDN;

    /**
     * The distinguised name with which to bind to LDAP. Optional.
     */
    private String bindDN;

    /**
     * The password to use when binding to LDAP. Optional.
     */
    private String bindPassword;

    /**
     * The LDAP attribute which stores the user's email address. Typically
     * 'mail' or something similar to that.
     */
    private String emailAttribute;

    /**
     * The class name of the InitialContextFactory implementation to use when
     * connecting to LDAP.
     */
    private String initialContextFactoryName;

    /**
     * If true, the user's LDAP account will be found by searching through the
     * baseDN
     */
    private boolean performSearch = false;

    /**
     * The LDAP attribute to use for searching. Usually 'uid'
     */
    private String searchAttribute;

    /**
     * The LDAP server's URL.
     */
    private String server;

    public String getBaseDN() {
        return baseDN;
    }

    public String getBindDN() {
        return bindDN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public String getEmailAttribute() {
        return emailAttribute;
    }

    public String getInitialContextFactoryName() {
        return initialContextFactoryName != null ? initialContextFactoryName
                : DEFAULT_INITIAL_CONTEXT_FACTORY;
    }

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public String getServer() {
        return server;
    }

    public boolean isBindCredentialsProvided() {
        return (bindDN != null) && (bindPassword != null);
    }

    public boolean isPerformSearch() {
        return performSearch;
    }

    public boolean isValid() {
        return (server != null) && (baseDN != null) && (searchAttribute != null)
                && (emailAttribute != null);
    }

    /**
     * Construct a user's distinguised name (DN) from their username.
     * 
     * @param userName the user's username
     * @return the DN
     */
    public String makeUserDN(String userName) {
        StringBuilder builder = new StringBuilder();
        builder.append(getSearchAttribute()).append("=").append(userName);
        builder.append(",").append(getBaseDN());
        return builder.toString();
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public void setBindDN(String bindDN) {
        this.bindDN = bindDN;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public void setEmailAttribute(String emailAttribute) {
        this.emailAttribute = emailAttribute;
    }

    public void setInitialContextFactoryName(String initialContextFactoryName) {
        this.initialContextFactoryName = initialContextFactoryName;
    }

    public void setPerformSearch(boolean performSearch) {
        this.performSearch = performSearch;
    }

    public void setSearchAttribute(String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public void setServer(String server) {
        this.server = server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("baseDN", baseDN)
                .append("bindDN", bindDN).append("bindPassword",
                        bindPassword != null ? "xxxx" : null).append("emailAttribute",
                        emailAttribute).append("initialContextFactoryName",
                        initialContextFactoryName).append("performSearch", performSearch).append(
                        "server", server).toString();
    }

}
